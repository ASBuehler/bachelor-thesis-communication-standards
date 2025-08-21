// src/main/java/sqtest/InvoiceLineItem.java
package sqtest;

import java.math.BigDecimal;
import java.util.Objects;

public class InvoiceLineItem {
    private String productId;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Standardkonstruktor
    public InvoiceLineItem() {}

    public InvoiceLineItem(String productId, String description, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        // Berechne den Gesamtpreis der Position
        this.lineTotal = unitPrice.multiply(new BigDecimal(quantity));
    }

    // Getter & Setter
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceLineItem that = (InvoiceLineItem) o;
        // BigDecimal.compareTo() verwenden für den korrekten Vergleich
        return quantity == that.quantity &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(description, that.description) &&
               (unitPrice != null && unitPrice.compareTo(that.unitPrice) == 0) &&
               (lineTotal != null && lineTotal.compareTo(that.lineTotal) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, description, quantity, unitPrice, lineTotal);
    }
}