package com.novatech.store.orders;

import com.novatech.store.orders.OrderService.CheckoutInput;
import com.novatech.store.orders.OrderService.OrderView;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;
    private final InvoiceService invoices;
    public OrderController(OrderService orders, InvoiceService invoices) {
        this.orders = orders;
        this.invoices = invoices;
    }

    @PostMapping("/checkout")
    public OrderView checkout(JwtAuthenticationToken auth,@RequestBody CheckoutInput input) {
        return orders.checkout(Long.valueOf(auth.getName()), input.key());
    }

    @GetMapping
    public List<OrderView> list(JwtAuthenticationToken auth) {
        return orders.list(Long.valueOf(auth.getName()));
    }

    @GetMapping("/{id}")
    public OrderView get(JwtAuthenticationToken auth,@PathVariable Long id) {
        return orders.find(Long.valueOf(auth.getName()), id);
    }

    @PostMapping("/{id}/pay-demo")
    public OrderView pay(JwtAuthenticationToken auth,@PathVariable Long id) {
        return orders.payDemo(Long.valueOf(auth.getName()), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderView cancel(JwtAuthenticationToken auth,@PathVariable Long id) {
        return orders.cancel(Long.valueOf(auth.getName()), id);
    }

    @GetMapping(value = "/{id}/receipt.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> receipt(JwtAuthenticationToken auth,@PathVariable Long id) {
        byte[] pdf = invoices.receipt(orders.find(Long.valueOf(auth.getName()), id));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novatech-pedido-" + id + ".pdf").body(pdf);
    }
}
