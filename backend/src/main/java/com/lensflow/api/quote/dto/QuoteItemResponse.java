
package com.lensflow.api.quote.dto;

import java.math.BigDecimal;

public class QuoteItemResponse {

    private final String description;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal total;

    public QuoteItemResponse(
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal total
    ) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.total = total;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
