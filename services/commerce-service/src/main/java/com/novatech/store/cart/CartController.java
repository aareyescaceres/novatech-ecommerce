package com.novatech.store.cart;

import com.novatech.store.cart.CartService.CartView;
import com.novatech.store.cart.CartService.PutItem;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;
    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public CartView view(JwtAuthenticationToken auth) {
        return service.view(Long.valueOf(auth.getName()));
    }

    @PutMapping("/items")
    public CartView put(JwtAuthenticationToken auth,@Valid@RequestBody PutItem input) {
        return service.put(Long.valueOf(auth.getName()), input.productId(), input.quantity());
    }
}
