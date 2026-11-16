
package com.lensflow.api.payment.dto;

import com.lensflow.api.payment.entity.PaymentMethod;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentResponse {

    private final Long id;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final PaymentMethod method;
    private final PaymentType type;
    private final PaymentStatus status;
    private final String notes;
    private final Long shootingId;
    private final String shootingTitle;
    private final LocalDateTime createdAt;

    public PaymentResponse(
            Long id,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentMethod method,
            PaymentType type,
            PaymentStatus status,
            String notes,
            Long shootingId,
            String shootingTitle,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.type = type;
        this.status = status;
        this.notes = notes;
        this.shootingId = shootingId;
        this.shootingTitle = shootingTitle;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentType getType() {
        return type;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Long getShootingId() {
        return shootingId;
    }

    public String getShootingTitle() {
        return shootingTitle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
