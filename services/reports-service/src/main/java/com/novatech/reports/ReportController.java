package com.novatech.reports;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Servicio interno: no se llama desde el navegador. */
@RestController
@RequestMapping("/internal/reports")
public class ReportController {
    private final ReportService reports;
    private final String internalToken;

    public ReportController(
            ReportService reports,
            @Value("${reports.internal-token}") String internalToken
    ) {
        this.reports = reports;
        this.internalToken = internalToken;
    }

    @GetMapping("/summary")
    public ReportService.Summary summary(@RequestHeader(value = "X-Internal-Token", required = false) String token) {
        authorize(token);
        return reports.summary();
    }

    @GetMapping(value = "/store.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel(
            @RequestHeader(value = "X-Internal-Token", required = false) String token
    ) throws IOException {
        authorize(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novatech-reporte.xlsx")
                .body(reports.workbook());
    }

    private void authorize(String supplied) {
        byte[] actual = supplied == null ? new byte[0] : supplied.getBytes(StandardCharsets.UTF_8);
        byte[] expected = internalToken.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(actual, expected)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token interno no válido");
        }
    }
}
