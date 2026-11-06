package com.novatech.backoffice.reports;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportsController {
    private final ReportClient reports;

    public ReportsController(ReportClient reports) { this.reports = reports; }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ResponseEntity<byte[]> summary() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(reports.summary());
    }

    @GetMapping(value = "/store.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ResponseEntity<byte[]> excel() {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novatech-reporte.xlsx")
            .body(reports.workbook());
    }
}
