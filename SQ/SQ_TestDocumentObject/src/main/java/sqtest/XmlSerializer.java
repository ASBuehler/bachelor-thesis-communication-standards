package sqtest;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XmlSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T object) throws Exception {
        if (object instanceof Invoice invoice) {
            XmlInvoice xmlInvoice = convertToXmlDto(invoice);
            
            JAXBContext context = JAXBContext.newInstance(XmlInvoice.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            marshaller.marshal(xmlInvoice, out);
            return out.toByteArray();
        }
        throw new IllegalArgumentException("XML serialization for type " + object.getClass().getName() + " is not implemented.");
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (clazz.equals(Invoice.class)) {
            JAXBContext context = JAXBContext.newInstance(XmlInvoice.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            XmlInvoice xmlInvoice = (XmlInvoice) unmarshaller.unmarshal(new ByteArrayInputStream(data));
            return (T) convertFromXmlDto(xmlInvoice);
        }
        throw new IllegalArgumentException("XML deserialization for type " + clazz.getName() + " is not implemented.");
    }

    // --- DTO zu POJO Konvertierungs-Helfer ---

    private XmlInvoice convertToXmlDto(Invoice original) {
        if (original == null) return null;
        XmlInvoice dto = new XmlInvoice();
        dto.invoiceNumber = original.getInvoiceNumber();
        dto.issueDate = original.getIssueDate();
        dto.dueDate = original.getDueDate();
        dto.billingAddress = original.getBillingAddress();
        dto.status = original.getStatus();
        dto.notes = original.getNotes();
        dto.companyLogo = original.getCompanyLogo();

        // BigDecimal zu String
        dto.subtotal = (original.getSubtotal() != null) ? original.getSubtotal().toPlainString() : null;
        dto.tax = (original.getTax() != null) ? original.getTax().toPlainString() : null;
        dto.total = (original.getTotal() != null) ? original.getTotal().toPlainString() : null;

        if (original.getLineItems() != null) {
            dto.lineItems = original.getLineItems().stream()
                .map(this::convertToXmlDto)
                .collect(Collectors.toList());
        }
        return dto;
    }

    private XmlInvoiceLineItem convertToXmlDto(InvoiceLineItem original) {
        if (original == null) return null;
        XmlInvoiceLineItem dto = new XmlInvoiceLineItem();
        dto.productId = original.getProductId();
        dto.description = original.getDescription();
        dto.quantity = original.getQuantity();
        dto.unitPrice = (original.getUnitPrice() != null) ? original.getUnitPrice().toPlainString() : null;
        dto.lineTotal = (original.getLineTotal() != null) ? original.getLineTotal().toPlainString() : null;
        return dto;
    }

    // --- POJO zu DTO Konvertierungs-Helfer ---

    private Invoice convertFromXmlDto(XmlInvoice dto) {
        if (dto == null) return null;
        Invoice original = new Invoice();
        original.setInvoiceNumber(dto.invoiceNumber);
        original.setIssueDate(dto.issueDate);
        original.setDueDate(dto.dueDate);
        original.setBillingAddress(dto.billingAddress);
        original.setStatus(dto.status);
        original.setNotes(dto.notes);
        original.setCompanyLogo(dto.companyLogo);

        // String zu BigDecimal
        original.setSubtotal((dto.subtotal != null) ? new BigDecimal(dto.subtotal) : null);
        original.setTax((dto.tax != null) ? new BigDecimal(dto.tax) : null);
        original.setTotal((dto.total != null) ? new BigDecimal(dto.total) : null);

        if (dto.lineItems != null) {
            original.setLineItems(dto.lineItems.stream()
                .map(this::convertFromXmlDto)
                .collect(Collectors.toList()));
        }
        return original;
    }

    private InvoiceLineItem convertFromXmlDto(XmlInvoiceLineItem dto) {
        if (dto == null) return null;
        InvoiceLineItem original = new InvoiceLineItem();
        original.setProductId(dto.productId);
        original.setDescription(dto.description);
        original.setQuantity(dto.quantity);
        original.setUnitPrice((dto.unitPrice != null) ? new BigDecimal(dto.unitPrice) : null);
        original.setLineTotal((dto.lineTotal != null) ? new BigDecimal(dto.lineTotal) : null);
        return original;
    }

    // =================================================================
    // Innere DTO-Klassen zur Kapselung der XML-spezifischen Logik
    // =================================================================
    
    @XmlRootElement(name = "invoice")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlInvoice {
        private String invoiceNumber;
        @XmlJavaTypeAdapter(LocalDateAdapter.class) private LocalDate issueDate;
        @XmlJavaTypeAdapter(LocalDateAdapter.class) private LocalDate dueDate;
        private Address billingAddress;
        @XmlElementWrapper(name = "lineItems") @XmlElement(name = "item") private List<XmlInvoiceLineItem> lineItems = new ArrayList<>();
        private String subtotal;
        private String tax;
        private String total;
        private PaymentStatus status;
        private String notes;
        private byte[] companyLogo;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlInvoiceLineItem {
        private String productId;
        private String description;
        private int quantity;
        private String unitPrice;
        private String lineTotal;
    }
}