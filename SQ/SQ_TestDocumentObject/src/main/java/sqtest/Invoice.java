// src/main/java/sqtest/Invoice.java
package sqtest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class Invoice {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Address billingAddress;
    private List<InvoiceLineItem> lineItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private PaymentStatus status;
    private String notes; // Langes Textfeld
    private byte[] companyLogo; // Binärdaten

    // Standardkonstruktor
    public Invoice() {}

    // Komplexer Konstruktor zum Erstellen von Testdaten
    public Invoice(String invoiceNumber, LocalDate issueDate, LocalDate dueDate, Address billingAddress, List<InvoiceLineItem> lineItems, BigDecimal taxRate, String notes, byte[] companyLogo) {
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.billingAddress = billingAddress;
        this.lineItems = lineItems;
        this.notes = notes;
        this.companyLogo = companyLogo;
        this.status = PaymentStatus.DRAFT; // Standardstatus

        // Berechnungen durchführen
        this.subtotal = lineItems.stream()
                                  .map(InvoiceLineItem::getLineTotal)
                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.tax = this.subtotal.multiply(taxRate);
        this.total = this.subtotal.add(this.tax);
    }

    // Getter & Setter
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Address getBillingAddress() { return billingAddress; }
    public void setBillingAddress(Address billingAddress) { this.billingAddress = billingAddress; }
    public List<InvoiceLineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<InvoiceLineItem> lineItems) { this.lineItems = lineItems; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public byte[] getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(byte[] companyLogo) { this.companyLogo = companyLogo; }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;

        // Funktion zum sicheren Vergleich von BigDecimal-Werten
        // Gibt true zurück, wenn beide null sind oder wenn ihre numerischen Werte gleich sind.
        BiPredicate<BigDecimal, BigDecimal> bigDecimalEquals = (bd1, bd2) -> {
            if (bd1 == null && bd2 == null) return true;
            if (bd1 == null || bd2 == null) return false;
            return bd1.compareTo(bd2) == 0;
        };

        return Objects.equals(invoiceNumber, invoice.invoiceNumber) &&
            Objects.equals(issueDate, invoice.issueDate) &&
            Objects.equals(dueDate, invoice.dueDate) &&
            Objects.equals(billingAddress, invoice.billingAddress) &&
            Objects.equals(lineItems, invoice.lineItems) &&
            bigDecimalEquals.test(subtotal, invoice.subtotal) &&
            bigDecimalEquals.test(tax, invoice.tax) &&
            bigDecimalEquals.test(total, invoice.total) &&
            status == invoice.status &&
            Objects.equals(notes, invoice.notes) &&
            java.util.Arrays.equals(companyLogo, invoice.companyLogo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(invoiceNumber, issueDate, dueDate, billingAddress, lineItems, subtotal, tax, total, status, notes);
        result = 31 * result + java.util.Arrays.hashCode(companyLogo);
        return result;
    }
}