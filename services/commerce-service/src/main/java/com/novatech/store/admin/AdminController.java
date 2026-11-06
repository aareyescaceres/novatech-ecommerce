package com.novatech.store.admin;

import com.novatech.store.catalog.CatalogService;
import com.novatech.store.catalog.CatalogService.ProductInput;
import com.novatech.store.catalog.CatalogService.ProductView;
import com.novatech.store.catalog.CatalogService.StockInput;
import com.novatech.store.orders.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Operaciones comerciales internas; nunca llamadas directamente por los navegadores. */
@RestController
@RequestMapping("/internal/commerce/admin")
public class AdminController {
    private final CatalogService catalog;
    private final OrderService orders;
    private final InternalRequestAuth internalAuth;

    public AdminController(CatalogService catalog, OrderService orders, InternalRequestAuth internalAuth) {
        this.catalog = catalog;
        this.orders = orders;
        this.internalAuth = internalAuth;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER','SALES_STAFF')")
    public List<ProductView> products(@RequestHeader(value = "X-Internal-Token", required = false) String secret) {
        internalAuth.check(secret);
        return catalog.allAdmin();
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ProductView create(@RequestHeader(value = "X-Internal-Token", required = false) String secret,
                              @Valid @RequestBody ProductInput input) {
        internalAuth.check(secret);
        return catalog.create(input);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ProductView edit(@RequestHeader(value = "X-Internal-Token", required = false) String secret,
                            @PathVariable Long id, @Valid @RequestBody ProductInput input) {
        internalAuth.check(secret);
        return catalog.update(id, input);
    }

    @PatchMapping("/products/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ProductView stock(@RequestHeader(value = "X-Internal-Token", required = false) String secret,
                             @PathVariable Long id, @Valid @RequestBody StockInput input) {
        internalAuth.check(secret);
        return catalog.setStock(id, input.stock());
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public List<OrderService.OrderView> orders(@RequestHeader(value = "X-Internal-Token", required = false) String secret) {
        internalAuth.check(secret);
        return orders.allAdmin();
    }

    @PostMapping("/orders/{id}/refund-demo")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public OrderService.OrderView refund(@RequestHeader(value = "X-Internal-Token", required = false) String secret,
                                         @PathVariable Long id) {
        internalAuth.check(secret);
        return orders.refundDemo(id);
    }
}
