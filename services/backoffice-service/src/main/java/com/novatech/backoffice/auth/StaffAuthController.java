package com.novatech.backoffice.auth;

import com.novatech.backoffice.clients.CommerceClient;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Login de empleados: verifica la firma del JWT emitido por commerce y el rol. */
@RestController
@RequestMapping("/api/auth")
public class StaffAuthController {
    private static final Pattern TOKEN = Pattern.compile("\"token\"\\s*:\\s*\"([A-Za-z0-9._-]+)\"");
    private final CommerceClient commerce;
    private final JwtDecoder decoder;

    public StaffAuthController(CommerceClient commerce, JwtDecoder decoder) {
        this.commerce = commerce;
        this.decoder = decoder;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> login(@RequestBody String json) {
        ResponseEntity<byte[]> result = commerce.login(json);
        if (!result.getStatusCode().is2xxSuccessful()) return result;
        if (result.getBody() == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        Matcher matcher = TOKEN.matcher(new String(result.getBody(), StandardCharsets.UTF_8));
        if (!matcher.find()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta de autenticacion invalida");
        Jwt jwt = decoder.decode(matcher.group(1));
        if (!isStaffRole(jwt.getClaimAsString("role")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta cuenta es de cliente, no del personal");
        return result;
    }

    public static boolean isStaffRole(String role) {
        return "ADMIN".equals(role) || "SALES_STAFF".equals(role) || "INVENTORY_MANAGER".equals(role);
    }
}
