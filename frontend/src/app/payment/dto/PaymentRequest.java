package com.lensflow.api.payment.dto;

import com.lensflow.api.payment.entity.PaymentMethod;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRequest {

    @NotNull(message = "Lo shooting è obbligatorio")
    @Positive(message = "L'ID dello shooting deve essere positivo")
    private Long shootingId;

    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero")
    @Digits(integer = 10, fraction = 2,
            message = "L'importo può avere massimo due cifre decimali")
    private BigDecimal amount;

    @NotNull(message = "La data del pagamento è obbligatoria")
    private LocalDate paymentDate;

    @NotNull(message = "Il metodo di pagamento è obbligatorio")
    private PaymentMethod method;

    @NotNull(message = "La tipologia del pagamento è obbligatoria")
    private PaymentType type;

    @NotNull(message = "Lo stato del pagamento è obbligatorio")
    private PaymentStatus status;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String notes;

    public PaymentRequest() {
    }

    public Long getShootingId() {
        return shootingId;
    }

    public void setShootingId(Long shootingId) {
        this.shootingId = shootingId;
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
}