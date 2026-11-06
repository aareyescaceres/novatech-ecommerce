package com.novatech.backoffice.clients;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** BFF: comunica la API comercial, reenvia JWT y añade credencial de red interna. */
@Service
public class CommerceClient {
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3)).build();
    private final URI baseUrl;
    private final String internalToken;

    public CommerceClient(@Value("${commerce.base-url}") String baseUrl,
                          @Value("${commerce.internal-token}") String internalToken) {
        this.baseUrl = URI.create(baseUrl);
        this.internalToken = internalToken;
    }

    /** Los paths son constantes del backend o usan identificadores numericos, nunca URLs del cliente. */
    public ResponseEntity<byte[]> admin(HttpMethod method, String path, String json, String bearer) {
        return exchange(method, "/internal/commerce/admin" + path, json, bearer, true);
    }

    /** Autenticacion delegada al servicio propietario de las cuentas. */
    public ResponseEntity<byte[]> login(String json) {
        return exchange(HttpMethod.POST, "/api/auth/login", json, null, false);
    }

    private ResponseEntity<byte[]> exchange(HttpMethod method, String path, String json,
                                             String bearer, boolean internal) {
        HttpRequest.Builder request = HttpRequest.newBuilder(baseUrl.resolve(path))
            .timeout(Duration.ofSeconds(15))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (internal) {
            request.header("X-Internal-Token", internalToken);
            if (bearer == null || !bearer.startsWith("Bearer "))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta token de personal");
            request.header(HttpHeaders.AUTHORIZATION, bearer);
        }
        if (json != null) request.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpRequest.BodyPublisher body = json == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
        request.method(method.name(), body);
        try {
            HttpResponse<byte[]> response = http.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            // Devuelve el estado HTTP de origen (403/404/409/422), nunca lo convierte en 200.
            return ResponseEntity.status(response.statusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Operacion interrumpida");
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Commerce no disponible; inicia StoreApplication");
        }
    }
}
