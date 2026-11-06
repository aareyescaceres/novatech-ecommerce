package com.novatech.store.orders;

import com.novatech.store.orders.OrderService.OrderLineView;
import com.novatech.store.orders.OrderService.OrderView;
import com.novatech.store.shared.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InvoiceServiceTest {
    private final InvoiceService service = new InvoiceService();
    private OrderView order(String status) {
        return new OrderView(7L, status, new BigDecimal("199.90"), Instant.now(), Instant.now(), Instant.now(), List.of(new OrderLineView("SKU-1", "Teclado", new BigDecimal("199.90"), 1, new BigDecimal("199.90"))));
    }

    @Test
    void paidOrderProducesAReadablePdf() throws Exception {
        byte[] bytes = service.receipt(order("PAID"));
        assertTrue(bytes.length>100);
        try (var pdf = Loader.loadPDF(bytes)) {
            String content = new PDFTextStripper().getText(pdf);
            assertTrue(content.contains("NOVATECH"));
            assertTrue(content.contains("199.90"));
        }
    }

    @Test
    void pendingOrderHasNoReceipt() {
        assertThrows(ApiException.class,() -> service.receipt(order("PENDING_PAYMENT")));
    }
}
