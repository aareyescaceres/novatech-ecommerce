package com.novatech.store.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalRequestAuth {
    private final byte[] expected;

    public InternalRequestAuth(@Value("${app.internal-token}") String token) {
        if (token.isBlank()) throw new IllegalArgumentException("COMMERCE_INTERNAL_TOKEN requerido");
        this.expected = token.getBytes(StandardCharsets.UTF_8);
    }

    public void check(String token) {
        byte[] supplied = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(supplied, expected))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credencial interna no valida");
    }
}
