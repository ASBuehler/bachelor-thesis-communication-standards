package sqtest;

import com.google.protobuf.ByteString;
import sqtest.proto.InvoiceProto;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class ProtobufSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof Invoice invoice) {
            InvoiceProto.Invoice protoInvoice = convertToProtoInvoice(invoice);
            return protoInvoice.toByteArray();
        }
        throw new IllegalArgumentException("Unsupported type for Protobuf: " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(Invoice.class)) {
            InvoiceProto.Invoice protoInvoice = InvoiceProto.Invoice.parseFrom(data);
            return (T) convertFromProtoInvoice(protoInvoice);
        }
        throw new IllegalArgumentException("Unsupported type for Protobuf: " + clazz.getName());
    }

    // --- Konvertierungs-Helfer ---

    private InvoiceProto.Invoice convertToProtoInvoice(Invoice original) {
        if (original == null) return null;
        InvoiceProto.Invoice.Builder builder = InvoiceProto.Invoice.newBuilder();

        if (original.getInvoiceNumber() != null) builder.setInvoiceNumber(original.getInvoiceNumber());
        if (original.getIssueDate() != null) builder.setIssueDate(original.getIssueDate().toString());
        if (original.getDueDate() != null) builder.setDueDate(original.getDueDate().toString());
        if (original.getBillingAddress() != null) {
            Address a = original.getBillingAddress();
            InvoiceProto.Address.Builder addrBuilder = InvoiceProto.Address.newBuilder();
            if (a.getStreet() != null) addrBuilder.setStreet(a.getStreet());
            if (a.getCity() != null) addrBuilder.setCity(a.getCity());
            if (a.getPostalCode() != null) addrBuilder.setPostalCode(a.getPostalCode());
            if (a.getCountry() != null) addrBuilder.setCountry(a.getCountry());
            builder.setBillingAddress(addrBuilder.build());
        }
        if (original.getLineItems() != null) {
            builder.addAllLineItems(original.getLineItems().stream().map(item -> {
                InvoiceProto.InvoiceLineItem.Builder itemBuilder = InvoiceProto.InvoiceLineItem.newBuilder();
                if (item.getProductId() != null) itemBuilder.setProductId(item.getProductId());
                if (item.getDescription() != null) itemBuilder.setDescription(item.getDescription());
                itemBuilder.setQuantity(item.getQuantity());
                if (item.getUnitPrice() != null) itemBuilder.setUnitPrice(item.getUnitPrice().toPlainString());
                if (item.getLineTotal() != null) itemBuilder.setLineTotal(item.getLineTotal().toPlainString());
                return itemBuilder.build();
            }).collect(Collectors.toList()));
        }
        if (original.getSubtotal() != null) builder.setSubtotal(original.getSubtotal().toPlainString());
        if (original.getTax() != null) builder.setTax(original.getTax().toPlainString());
        if (original.getTotal() != null) builder.setTotal(original.getTotal().toPlainString());
        if (original.getStatus() != null) {
            builder.setStatus(InvoiceProto.PaymentStatus.valueOf(original.getStatus().name()));
        }
        if (original.getNotes() != null) builder.setNotes(original.getNotes());
        if (original.getCompanyLogo() != null) builder.setCompanyLogo(ByteString.copyFrom(original.getCompanyLogo()));
        
        return builder.build();
    }

    private Invoice convertFromProtoInvoice(InvoiceProto.Invoice protoInvoice) {
        if (protoInvoice == null) return null;
        Invoice original = new Invoice();
        if (protoInvoice.hasInvoiceNumber()) original.setInvoiceNumber(protoInvoice.getInvoiceNumber());
        if (protoInvoice.hasIssueDate()) original.setIssueDate(java.time.LocalDate.parse(protoInvoice.getIssueDate()));
        if (protoInvoice.hasDueDate()) original.setDueDate(java.time.LocalDate.parse(protoInvoice.getDueDate()));
        if (protoInvoice.hasBillingAddress()) {
            InvoiceProto.Address pa = protoInvoice.getBillingAddress();
            original.setBillingAddress(new Address(pa.getStreet(), pa.getCity(), pa.getPostalCode(), pa.getCountry()));
        }
        original.setLineItems(protoInvoice.getLineItemsList().stream().map(item -> {
            InvoiceLineItem li = new InvoiceLineItem();
            if (item.hasProductId()) li.setProductId(item.getProductId());
            if (item.hasDescription()) li.setDescription(item.getDescription());
            li.setQuantity(item.getQuantity());
            if (item.hasUnitPrice()) li.setUnitPrice(new BigDecimal(item.getUnitPrice()));
            if (item.hasLineTotal()) li.setLineTotal(new BigDecimal(item.getLineTotal()));
            return li;
        }).collect(Collectors.toList()));
        if (protoInvoice.hasSubtotal()) original.setSubtotal(new BigDecimal(protoInvoice.getSubtotal()));
        if (protoInvoice.hasTax()) original.setTax(new BigDecimal(protoInvoice.getTax()));
        if (protoInvoice.hasTotal()) original.setTotal(new BigDecimal(protoInvoice.getTotal()));
        if (protoInvoice.hasStatus() && protoInvoice.getStatus() != InvoiceProto.PaymentStatus.PAYMENT_STATUS_UNSPECIFIED) {
             original.setStatus(PaymentStatus.valueOf(protoInvoice.getStatus().name()));
        }
        if (protoInvoice.hasNotes()) original.setNotes(protoInvoice.getNotes());
        if (protoInvoice.hasCompanyLogo()) original.setCompanyLogo(protoInvoice.getCompanyLogo().toByteArray());
        
        return original;
    }
}