package com.novatech.store.orders;

import com.novatech.store.cart.*;
import com.novatech.store.catalog.*;
import com.novatech.store.shared.ApiException;
import com.novatech.store.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    public record CheckoutInput(String key) {
    }

    public record OrderLineView(String sku, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record OrderView(Long id, String status, BigDecimal total, Instant createdAt, Instant expiresAt, Instant paidAt, List<OrderLineView> items) {
    }

    private final OrderRepository orders;
    private final CartRepository carts;
    private final ProductRepository products;
    private final UserRepository users;
    private final long timeoutMinutes;
    public OrderService(OrderRepository orders, CartRepository carts, ProductRepository products, UserRepository users,@Value("${app.payment-timeout-minutes}") long timeoutMinutes) {
        this.orders = orders;
        this.carts = carts;
        this.products = products;
        this.users = users;
        this.timeoutMinutes = timeoutMinutes;
    }

    @Transactional
    public OrderView checkout(Long userId, String key) {
        if (key == null ||!key.matches("[A-Za-z0-9_-]{8,100}"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Clave de compra invalida");
        // Se bloquea primero el carrito. Dos solicitudes del mismo cliente se serializan.
        Cart cart = carts.lockByUserId(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Carrito inexistente"));
        Optional<PurchaseOrder> repeated = orders.findByUserIdAndCheckoutKey(userId, key);
        if (repeated.isPresent())
            return viewOf(repeated.get());
        if (cart.items.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio");
        // Orden estable de bloqueo para reducir el riesgo de interbloqueos entre compradores.
        List<CartItem> requested = cart.items.stream().sorted(Comparator.comparing(i -> i.product.id)).toList();
        PurchaseOrder order = new PurchaseOrder();
        order.user = users.getReferenceById(userId);
        order.checkoutKey = key;
        order.status = "PENDING_PAYMENT";
        order.createdAt = Instant.now();
        order.expiresAt = order.createdAt.plus(timeoutMinutes, ChronoUnit.MINUTES);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item: requested) {
            Product locked = products.lockById(item.product.id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto inexistente"));
            if (!locked.active || locked.stock<item.quantity)
                throw new ApiException(HttpStatus.CONFLICT, "Stock insuficiente: " + locked.name);
            locked.stock -= item.quantity;
            // Protegido por lock y transaccion de PostgreSQL.
            OrderItem line = new OrderItem();
            line.order = order;
            line.product = locked;
            line.skuSnapshot = locked.sku;
            line.nameSnapshot = locked.name;
            line.unitPrice = locked.price;
            line.quantity = item.quantity;
            line.lineTotal = locked.price.multiply(BigDecimal.valueOf(item.quantity));
            order.items.add(line);
            total = total.add(line.lineTotal);
        }
        order.total = total;
        orders.saveAndFlush(order);
        cart.items.clear();
        return viewOf(order);
    }

    @Transactional
    public OrderView payDemo(Long userId, Long id) {
        PurchaseOrder order = ownedLocked(userId, id);
        if (order.status.equals("PAID"))
            return viewOf(order);
        // Confirmacion repetida: idempotente.
        if (!order.status.equals("PENDING_PAYMENT"))
            throw new ApiException(HttpStatus.CONFLICT, "El pedido ya no admite pago");
        if (!Instant.now().isBefore(order.expiresAt))
            throw new ApiException(HttpStatus.CONFLICT, "La reserva vencio; espera la liberacion del stock");
        order.status = "PAID";
        order.paidAt = Instant.now();
        return viewOf(order);
    }

    @Transactional
    public OrderView cancel(Long userId, Long id) {
        PurchaseOrder order = ownedLocked(userId, id);
        if (order.status.equals("CANCELLED"))
            return viewOf(order);
        if (!order.status.equals("PENDING_PAYMENT"))
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden cancelar pedidos pendientes");
        release(order, "CANCELLED");
        return viewOf(order);
    }
    // Devolucion administrativa ficticia: restaura stock, sin procesar dinero real.
    @Transactional
    public OrderView refundDemo(Long id) {
        PurchaseOrder order = orders.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido inexistente"));
        if (order.status.equals("REFUNDED"))
            return viewOf(order);
        if (!order.status.equals("PAID"))
            throw new ApiException(HttpStatus.CONFLICT, "Solo los pedidos pagados admiten devolucion");
        release(order, "REFUNDED");
        return viewOf(order);
    }

    @Transactional(readOnly = true)
    public List<OrderView> list(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::viewOf).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderView> allAdmin() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::viewOf).toList();
    }

    @Transactional(readOnly = true)
    public OrderView find(Long userId, Long id) {
        PurchaseOrder o = orders.findById(id).filter(x -> x.user.id.equals(userId)).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido inexistente"));
        return viewOf(o);
    }

    private PurchaseOrder ownedLocked(Long userId, Long id) {
        return orders.lockById(id).filter(x -> x.user.id.equals(userId)).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido inexistente"));
    }
    // La orden ya esta bloqueada; se bloquean los productos en orden por ID.
    private void release(PurchaseOrder order, String newStatus) {
        order.items.stream().sorted(Comparator.comparing(i -> i.product.id)).forEach(line -> {
            Product p = products.lockById(line.product.id).orElseThrow(); p.stock += line.quantity;
        }
        );
        order.status = newStatus;
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void expireUnpaid() {
        for (Long id: orders.expiredIds(Instant.now())) {
            PurchaseOrder order = orders.lockById(id).orElse(null);
            if (order != null && order.status.equals("PENDING_PAYMENT") && Instant.now().isAfter(order.expiresAt))
                release(order, "EXPIRED");
        }
    }

    private OrderView viewOf(PurchaseOrder o) {
        return new OrderView(o.id, o.status, o.total, o.createdAt, o.expiresAt, o.paidAt, o.items.stream().map(i -> new OrderLineView(i.skuSnapshot, i.nameSnapshot, i.unitPrice, i.quantity, i.lineTotal)).toList());
    }
}
