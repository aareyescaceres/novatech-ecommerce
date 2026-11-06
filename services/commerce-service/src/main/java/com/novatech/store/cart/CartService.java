package com.novatech.store.cart;

import com.novatech.store.catalog.Product;
import com.novatech.store.catalog.ProductRepository;
import com.novatech.store.shared.ApiException;
import com.novatech.store.user.UserRepository;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    public record PutItem(@NotNull Long productId,@Min(0)@Max(99) int quantity) {
    }

    public record Line(Long productId, String name, String sku, BigDecimal price, int quantity, int stock, BigDecimal subtotal) {
    }

    public record CartView(List<Line> items, BigDecimal total) {
    }

    private final CartRepository carts;
    private final ProductRepository products;
    private final UserRepository users;
    public CartService(CartRepository carts, ProductRepository products, UserRepository users) {
        this.carts = carts;
        this.products = products;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public CartView view(Long userId) {
        Cart cart = carts.findByUserId(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Carrito inexistente"));
        return describe(cart);
    }

    @Transactional
    public CartView put(Long userId, Long productId, int quantity) {
        Cart cart = carts.lockByUserId(userId).orElseGet(() -> {
            Cart c = new Cart(); c.user = users.getReferenceById(userId); return carts.saveAndFlush(c);
        }
        );
        CartItem existing = cart.items.stream().filter(i -> i.product.id.equals(productId)).findFirst().orElse(null);
        if (quantity == 0) {
            if (existing != null) cart.items.remove(existing);
            return describe(cart);
        }
        Product p = products.findById(productId).filter(x -> x.active).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto inexistente"));
        if (quantity>p.stock)
            throw new ApiException(HttpStatus.CONFLICT, "No hay stock suficiente");
        if (existing == null) {
            CartItem item = new CartItem();
            item.cart = cart;
            item.product = p;
            item.quantity = quantity;
            cart.items.add(item);
        }
        else existing.quantity = quantity;
        return describe(cart);
    }

    private CartView describe(Cart c) {
        List<Line> items = c.items.stream().map(i -> new Line(i.product.id, i.product.name, i.product.sku, i.product.price, i.quantity, i.product.stock, i.product.price.multiply(BigDecimal.valueOf(i.quantity)))).toList();
        BigDecimal total = items.stream().map(Line::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(items, total);
    }
}
