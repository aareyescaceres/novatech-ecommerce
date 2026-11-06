package com.novatech.backoffice.admin;

import com.novatech.backoffice.clients.CommerceClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Endpoints del panel interno. Sin repositorios: la logica transaccional vive en commerce. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CommerceClient commerce;

    public AdminController(CommerceClient commerce) {
        this.commerce = commerce;
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF','INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> products(@RequestHeader("Authorization") String jwt) {
        return commerce.admin(HttpMethod.GET, "/products", null, jwt);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> create(@RequestHeader("Authorization") String jwt, @RequestBody String json) {
        return commerce.admin(HttpMethod.POST, "/products", json, jwt);
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> update(@RequestHeader("Authorization") String jwt,
                                          @PathVariable Long id, @RequestBody String json) {
        return commerce.admin(HttpMethod.PUT, "/products/" + id, json, jwt);
    }

    @PatchMapping("/products/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN','INVENTORY_MANAGER')")
    public ResponseEntity<byte[]> stock(@RequestHeader("Authorization") String jwt,
                                         @PathVariable Long id, @RequestBody String json) {
        return commerce.admin(HttpMethod.PATCH, "/products/" + id + "/stock", json, jwt);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ResponseEntity<byte[]> orders(@RequestHeader("Authorization") String jwt) {
        return commerce.admin(HttpMethod.GET, "/orders", null, jwt);
    }

    @PostMapping("/orders/{id}/refund-demo")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ResponseEntity<byte[]> refund(@RequestHeader("Authorization") String jwt, @PathVariable Long id) {
        return commerce.admin(HttpMethod.POST, "/orders/" + id + "/refund-demo", null, jwt);
    }
}
