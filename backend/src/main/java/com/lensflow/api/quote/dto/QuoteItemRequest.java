
package com.lensflow.api.quote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class QuoteItemRequest {

    @NotBlank(message = "La descrizione della voce è obbligatoria")
    @Size(max = 200, message = "La descrizione non può superare 200 caratteri")
    private String description;

    @NotNull(message = "La quantità è obbligatoria")
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private Integer quantity;

    @NotNull(message = "Il prezzo unitario è obbligatorio")
    @DecimalMin(value = "0.00", message = "Il prezzo non può essere negativo")
    private BigDecimal unitPrice;

    public QuoteItemRequest() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
