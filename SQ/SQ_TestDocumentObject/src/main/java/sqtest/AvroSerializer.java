package sqtest;

import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import sqtest.avro.Invoice; 

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;

public class AvroSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof sqtest.Invoice invoice) {
            Invoice avroInvoice = convertToAvroInvoice(invoice);

            DatumWriter<Invoice> datumWriter = new SpecificDatumWriter<>(Invoice.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.write(avroInvoice, encoder);
            encoder.flush();
            return out.toByteArray();
        }
        throw new IllegalArgumentException("Unsupported type for Avro: " + object.getClass().getName());
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(sqtest.Invoice.class)) {
            DatumReader<Invoice> datumReader = new SpecificDatumReader<>(Invoice.class);
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            Invoice avroInvoice = datumReader.read(null, decoder);
            return (T) convertFromAvroInvoice(avroInvoice);
        }
        throw new IllegalArgumentException("Unsupported type for Avro: " + clazz.getName());
    }

    // --- Konvertierungs-Helfer ---

    private Invoice convertToAvroInvoice(sqtest.Invoice original) {
        if (original == null) return null;

        sqtest.avro.Address avroAddress = null;
        if (original.getBillingAddress() != null) {
            sqtest.Address a = original.getBillingAddress();
            avroAddress = sqtest.avro.Address.newBuilder()
                .setStreet(a.getStreet()).setCity(a.getCity())
                .setPostalCode(a.getPostalCode()).setCountry(a.getCountry())
                .build();
        }

        return Invoice.newBuilder()
            .setInvoiceNumber(original.getInvoiceNumber())
            .setIssueDate(original.getIssueDate())
            .setDueDate(original.getDueDate())
            .setBillingAddress(avroAddress)
            .setLineItems(original.getLineItems().stream().map(item -> 
                sqtest.avro.InvoiceLineItem.newBuilder()
                    .setProductId(item.getProductId())
                    .setDescription(item.getDescription())
                    .setQuantity(item.getQuantity())
                    .setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().toPlainString() : null)
                    .setLineTotal(item.getLineTotal() != null ? item.getLineTotal().toPlainString() : null)
                    .build()
            ).collect(Collectors.toList()))
            .setSubtotal(original.getSubtotal() != null ? original.getSubtotal().toPlainString() : null)
            .setTax(original.getTax() != null ? original.getTax().toPlainString() : null)
            .setTotal(original.getTotal() != null ? original.getTotal().toPlainString() : null)
            .setStatus(original.getStatus() != null ? sqtest.avro.PaymentStatus.valueOf(original.getStatus().name()) : null)
            .setNotes(original.getNotes())
            .setCompanyLogo(original.getCompanyLogo() != null ? ByteBuffer.wrap(original.getCompanyLogo()) : null)
            .build();
    }

    private sqtest.Invoice convertFromAvroInvoice(Invoice avroInvoice) {
        if (avroInvoice == null) return null;
        
        sqtest.Address address = null;
        if (avroInvoice.getBillingAddress() != null) {
            sqtest.avro.Address a = avroInvoice.getBillingAddress();
            address = new sqtest.Address(
                a.getStreet() != null ? a.getStreet().toString() : null,
                a.getCity() != null ? a.getCity().toString() : null,
                a.getPostalCode() != null ? a.getPostalCode().toString() : null,
                a.getCountry() != null ? a.getCountry().toString() : null
            );
        }
        
        sqtest.Invoice original = new sqtest.Invoice();
        original.setInvoiceNumber(avroInvoice.getInvoiceNumber() != null ? avroInvoice.getInvoiceNumber().toString() : null);
        original.setIssueDate(avroInvoice.getIssueDate());
        original.setDueDate(avroInvoice.getDueDate());
        original.setBillingAddress(address);
        original.setLineItems(avroInvoice.getLineItems().stream().map(item -> {
            InvoiceLineItem li = new InvoiceLineItem();
            li.setProductId(item.getProductId() != null ? item.getProductId().toString() : null);
            li.setDescription(item.getDescription() != null ? item.getDescription().toString() : null);
            li.setQuantity(item.getQuantity());
            li.setUnitPrice(item.getUnitPrice() != null ? new BigDecimal(item.getUnitPrice().toString()) : null);
            li.setLineTotal(item.getLineTotal() != null ? new BigDecimal(item.getLineTotal().toString()) : null);
            return li;
        }).collect(Collectors.toList()));
        original.setSubtotal(avroInvoice.getSubtotal() != null ? new BigDecimal(avroInvoice.getSubtotal().toString()) : null);
        original.setTax(avroInvoice.getTax() != null ? new BigDecimal(avroInvoice.getTax().toString()) : null);
        original.setTotal(avroInvoice.getTotal() != null ? new BigDecimal(avroInvoice.getTotal().toString()) : null);
        original.setStatus(avroInvoice.getStatus() != null ? PaymentStatus.valueOf(avroInvoice.getStatus().name()) : null);
        original.setNotes(avroInvoice.getNotes() != null ? avroInvoice.getNotes().toString() : null);
        original.setCompanyLogo(avroInvoice.getCompanyLogo() != null ? avroInvoice.getCompanyLogo().array() : null);
        
        return original;
    }
}