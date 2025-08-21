package sqtest;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvSerializer implements Serializer {

    private static final String CSV_HEADER = "invoiceNumber,issueDate,dueDate,billingStreet,billingCity,lineProductId,lineDescription,lineQuantity,lineUnitPrice,lineTotal";

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Invoice invoice)) {
            throw new IllegalArgumentException("CSV serialization in this test is only supported for Invoice objects.");
        }

        
        StringBuilder sb = new StringBuilder();
        sb.append(CSV_HEADER).append("\n");

        if (invoice.getLineItems() == null || invoice.getLineItems().isEmpty()) {
            // Fall: Rechnung ohne Positionen
            String[] invoiceData = {
                escape(invoice.getInvoiceNumber()),
                escape(invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : ""),
                escape(invoice.getDueDate() != null ? invoice.getDueDate().toString() : ""),
                escape(invoice.getBillingAddress() != null ? invoice.getBillingAddress().getStreet() : ""),
                escape(invoice.getBillingAddress() != null ? invoice.getBillingAddress().getCity() : ""),
                "", "", "", "", "" // Leere Positionsdaten
            };
            sb.append(String.join(",", invoiceData)).append("\n");
        } else {
            // Fall: Rechnung mit Positionen (Normalfall)
            for (InvoiceLineItem item : invoice.getLineItems()) {
                String[] fullLine = {
                    escape(invoice.getInvoiceNumber()),
                    escape(invoice.getIssueDate() != null ? invoice.getIssueDate().toString() : ""),
                    escape(invoice.getDueDate() != null ? invoice.getDueDate().toString() : ""),
                    escape(invoice.getBillingAddress() != null ? invoice.getBillingAddress().getStreet() : ""),
                    escape(invoice.getBillingAddress() != null ? invoice.getBillingAddress().getCity() : ""),
                    escape(item.getProductId()),
                    escape(item.getDescription()),
                    String.valueOf(item.getQuantity()),
                    escape(item.getUnitPrice() != null ? item.getUnitPrice().toPlainString() : ""),
                    escape(item.getLineTotal() != null ? item.getLineTotal().toPlainString() : "")
                };
                sb.append(String.join(",", fullLine)).append("\n");
            }
        }
        
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Invoice.class)) {
            throw new IllegalArgumentException("CSV deserialization in this test is only supported for Invoice objects.");
        }
        
    
        String csvContent = new String(data, StandardCharsets.UTF_8);
        String[] lines = csvContent.split("\n");
        
        if (lines.length < 2) {
            throw new IllegalStateException("CSV data is empty or has no data rows.");
        }

        String[] parts = lines[1].split(",", -1); // Erste Datenzeile
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(parts[0]);
        if (!parts[1].isEmpty()) invoice.setIssueDate(java.time.LocalDate.parse(parts[1]));
        if (!parts[2].isEmpty()) invoice.setDueDate(java.time.LocalDate.parse(parts[2]));
        
        Address address = new Address();
        address.setStreet(parts[3]);
        address.setCity(parts[4]);
        invoice.setBillingAddress(address);

        List<InvoiceLineItem> items = new ArrayList<>();
        if (!parts[5].isEmpty()) {
             InvoiceLineItem item = new InvoiceLineItem();
             item.setProductId(parts[5]);
             item.setDescription(parts[6]);
             item.setQuantity(Integer.parseInt(parts[7]));
             if (!parts[8].isEmpty()) item.setUnitPrice(new BigDecimal(parts[8]));
             if (!parts[9].isEmpty()) item.setLineTotal(new BigDecimal(parts[9]));
             items.add(item);
        }
        invoice.setLineItems(items);

        return (T) invoice;
    }

    // Einfache Escape-Funktion für CSV, um Kommas und Anführungszeichen zu behandeln
    private String escape(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}