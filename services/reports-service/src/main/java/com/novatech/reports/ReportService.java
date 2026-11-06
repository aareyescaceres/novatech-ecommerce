package com.novatech.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    public record Summary(long paidOrders, BigDecimal sales, long unitsInStock, long activeProducts) {
    }

    private final JdbcTemplate jdbc;
    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Summary summary() {
        Long orders = jdbc.queryForObject("SELECT COUNT(*) FROM purchase_orders WHERE status='PAID'", Long.class);
        BigDecimal sales = jdbc.queryForObject("SELECT COALESCE(SUM(total),0) FROM purchase_orders WHERE status='PAID'", BigDecimal.class);
        Long units = jdbc.queryForObject("SELECT COALESCE(SUM(stock),0) FROM products WHERE active = true", Long.class);
        Long active = jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE active = true", Long.class);
        return new Summary(orders, sales, units, active);
    }

    public byte[] workbook() throws IOException {
        try (XSSFWorkbook book = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle head = book.createCellStyle();
            Font font = book.createFont();
            font.setBold(true);
            head.setFont(font);
            Sheet inventory = book.createSheet("Inventario");
            addRow(inventory, 0, head, "SKU", "Producto", "Marca", "Categoria", "Precio S/", "Stock", "Activo");
            List<Map<String, Object>> all = jdbc.queryForList("""
                    SELECT sku, name, brand, category, price, stock, active
                    FROM products ORDER BY id
                    """);
            for (int i = 0; i < all.size(); i++) {
                Map<String, Object> product = all.get(i);
                Row row = inventory.createRow(i + 1);
                put(row, 0, (String) product.get("sku"));
                put(row, 1, (String) product.get("name"));
                put(row, 2, (String) product.get("brand"));
                put(row, 3, (String) product.get("category"));
                put(row, 4, ((BigDecimal) product.get("price")).doubleValue());
                put(row, 5, ((Number) product.get("stock")).intValue());
                put(row, 6, (Boolean.TRUE.equals(product.get("active")) ? "Si" : "No"));
            }
            Sheet sales = book.createSheet("Pedidos pagados");
            addRow(sales, 0, head, "Pedido", "Fecha UTC", "SKU", "Producto", "Cantidad", "Precio S/", "Importe S/");
            List<Map<String, Object>> lines = jdbc.queryForList("""
                SELECT o.id AS pedido, CAST(o.created_at AS VARCHAR) AS fecha,
                       i.sku_snapshot AS sku, i.name_snapshot AS producto,
                       i.quantity AS cantidad, i.unit_price AS precio, i.line_total AS importe
                FROM purchase_orders o JOIN order_items i ON o.id=i.order_id
                WHERE o.status='PAID' ORDER BY o.created_at DESC, o.id DESC
                """);
            int rowIndex = 1;
            for (Map<String, Object> line: lines) {
                Row r = sales.createRow(rowIndex++);
                put(r, 0,((Number)line.get("pedido")).longValue());
                put(r, 1,(String)line.get("fecha"));
                put(r, 2,(String)line.get("sku"));
                put(r, 3,(String)line.get("producto"));
                put(r, 4,((Number)line.get("cantidad")).intValue());
                put(r, 5,((BigDecimal)line.get("precio")).doubleValue());
                put(r, 6,((BigDecimal)line.get("importe")).doubleValue());
            }
            for (Sheet s: List.of(inventory, sales)) {
                s.createFreezePane(0, 1);
                for (int c = 0; c<7; c++) s.setColumnWidth(c, Math.min(10000, c == 1 ? 9500: 5500));
                s.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(1, s.getLastRowNum()), 0, 6));
            }
            book.write(output);
            return output.toByteArray();
        }
    }

    private void addRow(Sheet sheet, int rowIndex, CellStyle style, String... labels) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i<labels.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(labels[i]);
            cell.setCellStyle(style);
        }
    }

    private void put(Row row, int col, String value) {
        // Evita que Excel ejecute formulas provenientes del catalogo.
        if (value != null &&!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) value = "'" + value;
        row.createCell(col).setCellValue(value == null ? "": value);
    }

    private void put(Row row, int col, double value) {
        row.createCell(col).setCellValue(value);
    }
}
