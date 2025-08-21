package sqtest;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IniSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (!(object instanceof Invoice invoice)) {
            throw new IllegalArgumentException("INI serialization in this test is only supported for Invoice objects.");
        }

        Ini ini = new Ini();
        
        // Header-Daten in die [invoice] Sektion schreiben
        Section invoiceSection = ini.add("invoice");
        invoiceSection.put("invoiceNumber", invoice.getInvoiceNumber());
        if (invoice.getIssueDate() != null) invoiceSection.put("issueDate", invoice.getIssueDate().toString());
        if (invoice.getDueDate() != null) invoiceSection.put("dueDate", invoice.getDueDate().toString());

        // Adress-Daten in die [billingAddress] Sektion schreiben
        if (invoice.getBillingAddress() != null) {
            Section addressSection = ini.add("billingAddress");
            addressSection.put("street", invoice.getBillingAddress().getStreet());
            addressSection.put("city", invoice.getBillingAddress().getCity());
        }

        // Jede Rechnungsposition erhält eine eigene, nummerierte Sektion
        if (invoice.getLineItems() != null) {
            int i = 1;
            for (InvoiceLineItem item : invoice.getLineItems()) {
                Section itemSection = ini.add("lineItem_" + i);
                itemSection.put("productId", item.getProductId());
                itemSection.put("description", item.getDescription());
                itemSection.put("quantity", String.valueOf(item.getQuantity()));
                if (item.getUnitPrice() != null) itemSection.put("unitPrice", item.getUnitPrice().toPlainString());
                i++;
            }
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ini.store(out);
        return out.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (!clazz.equals(Invoice.class)) {
            throw new IllegalArgumentException("INI deserialization in this test is only supported for Invoice objects.");
        }
        
        
        Ini ini = new Ini(new ByteArrayInputStream(data));
        
        Invoice invoice = new Invoice();
        Section invoiceSection = ini.get("invoice");
        invoice.setInvoiceNumber(invoiceSection.get("invoiceNumber"));
        if (invoiceSection.containsKey("issueDate")) invoice.setIssueDate(java.time.LocalDate.parse(invoiceSection.get("issueDate")));
        
        Section addressSection = ini.get("billingAddress");
        if (addressSection != null) {
            invoice.setBillingAddress(new Address(addressSection.get("street"), addressSection.get("city"), null, null));
        }

        List<InvoiceLineItem> items = new ArrayList<>();
        // Suche nach der ersten Rechnungsposition
        Section itemSection = ini.get("lineItem_1");
        if (itemSection != null) {
            InvoiceLineItem item = new InvoiceLineItem();
            item.setProductId(itemSection.get("productId"));
            item.setDescription(itemSection.get("description"));
            item.setQuantity(Integer.parseInt(itemSection.get("quantity")));
            if (itemSection.containsKey("unitPrice")) item.setUnitPrice(new BigDecimal(itemSection.get("unitPrice")));
            items.add(item);
        }
        invoice.setLineItems(items);

        return (T) invoice;
    }
}