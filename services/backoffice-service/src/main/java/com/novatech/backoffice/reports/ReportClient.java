package com.novatech.backoffice.reports;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final URI baseUrl;
    private final String internalToken;

    public ReportClient(@Value("${reports.base-url}") String url,
                        @Value("${reports.internal-token}") String internalToken) {
        this.baseUrl = URI.create(url);
        this.internalToken = internalToken;
    }

    public byte[] summary() { return get("/internal/reports/summary"); }
    public byte[] workbook() { return get("/internal/reports/store.xlsx"); }

    private byte[] get(String path) {
        HttpRequest request = HttpRequest.newBuilder(baseUrl.resolve(path))
            .header("X-Internal-Token", internalToken).timeout(Duration.ofSeconds(25)).GET().build();
        try {
            HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Respuesta incorrecta del servicio de reportes");
            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Consulta interrumpida");
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Reportes no disponibles; inicia ReportsApplication");
        }
    }
}
