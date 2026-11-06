package com.novatech.store.user;

import com.novatech.store.cart.Cart;
import com.novatech.store.cart.CartRepository;
import com.novatech.store.security.JwtService;
import com.novatech.store.shared.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record Register(@NotBlank@Email String email,@NotBlank@Size(min = 8, max = 72) String password) {
    }

    public record Login(@NotBlank String email,@NotBlank String password) {
    }

    public record Session(String token, Long id, String email, String role) {
    }

    private final UserRepository users;
    private final CartRepository carts;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    public AuthController(UserRepository users, CartRepository carts, PasswordEncoder passwords, JwtService jwt) {
        this.users = users;
        this.carts = carts;
        this.passwords = passwords;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    @Transactional
    public Session register(@Valid@RequestBody Register input) {
        String email = input.email().strip().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmail(email))
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya existe");
        AppUser u = new AppUser();
        u.email = email;
        u.passwordHash = passwords.encode(input.password());
        u = users.save(u);
        Cart c = new Cart();
        c.user = u;
        carts.save(c);
        return new Session(jwt.token(u), u.id, u.email, u.role);
    }

    @PostMapping("/login")
    public Session login(@Valid@RequestBody Login input) {
        AppUser u = users.findByEmail(input.email().strip().toLowerCase(java.util.Locale.ROOT)).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));
        if (!passwords.matches(input.password(), u.passwordHash))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        return new Session(jwt.token(u), u.id, u.email, u.role);
    }
}
