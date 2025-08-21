package sqtest;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TomlSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Invoice)) {
            throw new IllegalArgumentException("TOML serialization in this test is only supported for Invoice objects.");
        }

        // Schritt 1: Konvertiere das komplexe Invoice-Objekt in eine einfache Map,
        // die von toml4j sicher verarbeitet werden kann.
        Map<String, Object> tomlMap = convertInvoiceToMap((Invoice) object);

        TomlWriter tomlWriter = new TomlWriter();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos)) {
            tomlWriter.write(tomlMap, writer);
            writer.flush();
            return baos.toByteArray();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Invoice.class)) {
            throw new IllegalArgumentException("TOML deserialization in this test is only supported for Invoice objects.");
        }

        Toml toml = new Toml().read(new String(data));
        // Schritt 2: Konvertiere die von TOML gelesene Struktur zurück in ein Invoice-Objekt.
        return (T) convertTomlToInvoice(toml);
    }
    
    // =================================================================
    // Helfermethoden für die manuelle Konvertierung
    // =================================================================

    private Map<String, Object> convertInvoiceToMap(Invoice invoice) {
        Map<String, Object> map = new LinkedHashMap<>(); // LinkedHashMap behält die Reihenfolge bei

        map.put("invoiceNumber", invoice.getInvoiceNumber());
        // Konvertiere komplexe Typen in Strings
        if (invoice.getIssueDate() != null) map.put("issueDate", invoice.getIssueDate().toString());
        if (invoice.getDueDate() != null) map.put("dueDate", invoice.getDueDate().toString());
        if (invoice.getStatus() != null) map.put("status", invoice.getStatus().name());
        if (invoice.getNotes() != null) map.put("notes", invoice.getNotes());
        
        // Konvertiere BigDecimal in String
        if (invoice.getSubtotal() != null) map.put("subtotal", invoice.getSubtotal().toPlainString());
        if (invoice.getTax() != null) map.put("tax", invoice.getTax().toPlainString());
        if (invoice.getTotal() != null) map.put("total", invoice.getTotal().toPlainString());
        
        // Konvertiere byte[] in einen Base64-String
        if (invoice.getCompanyLogo() != null) map.put("companyLogo", Base64.getEncoder().encodeToString(invoice.getCompanyLogo()));
        
        // Verschachtelte Objekte als eigene Maps abbilden
        if (invoice.getBillingAddress() != null) {
            Map<String, Object> addrMap = new LinkedHashMap<>();
            addrMap.put("street", invoice.getBillingAddress().getStreet());
            addrMap.put("city", invoice.getBillingAddress().getCity());
            addrMap.put("postalCode", invoice.getBillingAddress().getPostalCode());
            addrMap.put("country", invoice.getBillingAddress().getCountry());
            map.put("billingAddress", addrMap);
        }

        // Liste von Objekten in eine Liste von Maps umwandeln
        if (invoice.getLineItems() != null) {
            List<Map<String, Object>> itemsList = invoice.getLineItems().stream()
                .map(this::convertLineItemToMap)
                .collect(Collectors.toList());
            map.put("lineItems", itemsList);
        }

        return map;
    }
    
    private Map<String, Object> convertLineItemToMap(InvoiceLineItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("productId", item.getProductId());
        map.put("description", item.getDescription());
        map.put("quantity", (long) item.getQuantity()); // TOML bevorzugt long für Ganzzahlen
        if (item.getUnitPrice() != null) map.put("unitPrice", item.getUnitPrice().toPlainString());
        if (item.getLineTotal() != null) map.put("lineTotal", item.getLineTotal().toPlainString());
        return map;
    }
    
    private Invoice convertTomlToInvoice(Toml toml) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(toml.getString("invoiceNumber"));
        if (toml.contains("issueDate")) invoice.setIssueDate(LocalDate.parse(toml.getString("issueDate")));
        if (toml.contains("dueDate")) invoice.setDueDate(LocalDate.parse(toml.getString("dueDate")));
        if (toml.contains("status")) invoice.setStatus(PaymentStatus.valueOf(toml.getString("status")));
        if (toml.contains("notes")) invoice.setNotes(toml.getString("notes"));

        if (toml.contains("subtotal")) invoice.setSubtotal(new BigDecimal(toml.getString("subtotal")));
        if (toml.contains("tax")) invoice.setTax(new BigDecimal(toml.getString("tax")));
        if (toml.contains("total")) invoice.setTotal(new BigDecimal(toml.getString("total")));
        
        if (toml.contains("companyLogo")) invoice.setCompanyLogo(Base64.getDecoder().decode(toml.getString("companyLogo")));
        
        if (toml.contains("billingAddress")) {
            Toml addrToml = toml.getTable("billingAddress");
            invoice.setBillingAddress(new Address(
                addrToml.getString("street"),
                addrToml.getString("city"),
                addrToml.getString("postalCode"),
                addrToml.getString("country")
            ));
        }

        if (toml.contains("lineItems")) {
            List<Toml> itemsToml = toml.getTables("lineItems");
            List<InvoiceLineItem> itemsList = new ArrayList<>();
            for (Toml itemToml : itemsToml) {
                InvoiceLineItem li = new InvoiceLineItem();
                li.setProductId(itemToml.getString("productId"));
                li.setDescription(itemToml.getString("description"));
                li.setQuantity(itemToml.getLong("quantity").intValue());
                if (itemToml.contains("unitPrice")) li.setUnitPrice(new BigDecimal(itemToml.getString("unitPrice")));
                if (itemToml.contains("lineTotal")) li.setLineTotal(new BigDecimal(itemToml.getString("lineTotal")));
                itemsList.add(li);
            }
            invoice.setLineItems(itemsList);
        }
        
        return invoice;
    }
}