package com.novatech.store.orders;

import com.novatech.store.orders.OrderService.OrderLineView;
import com.novatech.store.orders.OrderService.OrderView;
import com.novatech.store.shared.ApiException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {
    // PDF de demostracion: NO es comprobante tributario electronico.
    public byte[] receipt(OrderView order) {
        if (!order.status().equals("PAID"))
            throw new ApiException(HttpStatus.CONFLICT, "Solo los pedidos pagados tienen recibo");
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 790;
                y = write(cs, "NOVATECH STORE / RECIBO DE PRUEBA", 45, y, 15, true);
                y = write(cs, "No es boleta ni factura tributaria", 45, y - 8, 10, false);
                y = write(cs, "Pedido #" + order.id() + "  |  Estado: PAGADO", 45, y - 20, 11, false);
                y = write(cs, "Fecha UTC: " + order.createdAt(), 45, y - 4, 10, false);
                y -= 24;
                for (OrderLineView line: order.items()) {
                    String label = line.sku() + " | " + line.name();
                    if (label.length()>67) label = label.substring(0, 67);
                    y = write(cs, label, 45, y, 10, false);
                    y = write(cs, "Cantidad: " + line.quantity() + "  x S/ " + line.unitPrice() + " = S/ " + line.lineTotal(), 55, y, 10, false);
                    y -= 7;
                }
                y = write(cs, "TOTAL: S/ " + order.total(), 45, y - 12, 14, true);
                write(cs, "Pago simulado. Ningun cargo real fue realizado.", 45, y - 25, 10, false);
            }
            doc.save(out);
            return out.toByteArray();
        }
        catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar PDF");
        }
    }

    private float write(PDPageContentStream stream, String text, float x, float y, int size, boolean bold) throws IOException {
        if (y<50)
            throw new IOException("Demasiadas lineas para una pagina");
        stream.beginText();
        stream.setFont(new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD: Standard14Fonts.FontName.HELVETICA), size);
        stream.newLineAtOffset(x, y);
        stream.showText(text.replaceAll("[^\\x20-\\x7E]", "?"));
        stream.endText();
        return y -(size + 9);
    }
}
