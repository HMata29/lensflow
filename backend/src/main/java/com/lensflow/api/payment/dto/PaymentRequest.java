
package com.lensflow.api.payment.dto;

import com.lensflow.api.payment.entity.PaymentMethod;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.entity.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRequest {

    @NotNull(message = "L'importo è obbligatorio")
    @Positive(message = "L'importo deve essere maggiore di zero")
    private BigDecimal amount;

    @NotNull(message = "La data del pagamento è obbligatoria")
    private LocalDate paymentDate;

    @NotNull(message = "Il metodo di pagamento è obbligatorio")
    private PaymentMethod method;

    @NotNull(message = "La tipologia di pagamento è obbligatoria")
    private PaymentType type;

    @NotNull(message = "Lo stato del pagamento è obbligatorio")
    private PaymentStatus status;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String notes;

    @NotNull(message = "Lo shooting è obbligatorio")
    @Positive(message = "L'ID dello shooting deve essere positivo")
    private Long shootingId;

    public PaymentRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getShootingId() {
        return shootingId;
    }

    public void setShootingId(Long shootingId) {
        this.shootingId = shootingId;
    }
}
