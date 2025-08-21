package sqtest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Datei enthält die Data Transfer Objects (DTOs), die speziell für die
 * JAXB-Serialisierung des Invoice-Modells benötigt werden. Sie kapseln alle
 * XML-spezifischen Annotationen und behandeln komplexe Typen wie BigDecimal,
 * indem sie diese als String abbilden.
 */

@XmlRootElement(name = "invoice")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlInvoiceDtos {

    // Dieses Feld ist nur dazu da, um die DTO-Klassen in einer einzigen Datei zu gruppieren.
    // Die eigentlichen DTOs sind die inneren Klassen XmlInvoice und XmlInvoiceLineItem.

    /**
     * DTO für das Haupt-Invoice-Objekt.
     */
    @XmlRootElement(name = "invoice")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlInvoice {

        private String invoiceNumber;
        
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate issueDate;
        
        @XmlJavaTypeAdapter(LocalDateAdapter.class)
        private LocalDate dueDate;
        
        // Das Address-POJO kann direkt verwendet werden, da es einfache Felder hat.
        // JAXB kann es standardmäßig verarbeiten, solange es auch annotiert ist oder
        // der XmlAccessType es erlaubt. Zur Sicherheit könnte man auch ein XmlAddress-DTO erstellen.
        private Address billingAddress;
        
        @XmlElementWrapper(name = "lineItems")
        @XmlElement(name = "item")
        private List<XmlInvoiceLineItem> lineItems = new ArrayList<>();
        
        // BigDecimal-Felder werden als String abgebildet, um Präzision zu garantieren.
        private String subtotal;
        private String tax;
        private String total;
        
        private PaymentStatus status;
        private String notes;
        private byte[] companyLogo;

        // Getter und Setter sind für die manuelle Konvertierung im XmlSerializer nicht
        // zwingend erforderlich, aber eine gute Praxis.
        // (Hier aus Gründen der Kürze weggelassen, können aber hinzugefügt werden)
    }

    /**
     * DTO für eine einzelne Rechnungsposition (InvoiceLineItem).
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlInvoiceLineItem {
        
        private String productId;
        private String description;
        private int quantity;
        
        // BigDecimal-Felder werden als String abgebildet.
        private String unitPrice;
        private String lineTotal;

        
    }
}