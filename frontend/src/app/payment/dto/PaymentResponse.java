package com.lensflow.api.payment.dto;

import com.lensflow.api.payment.entity.PaymentMethod;
import com.lensflow.api.payment.entity.PaymentStatus;
import com.lensflow.api.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentResponse {

    private final Long id;
    private final Long shootingId;
    private final String shootingTitle;
    private final String clientFirstName;
    private final String clientLastName;
    private final BigDecimal quoteTotalAmount;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final PaymentMethod method;
    private final PaymentType type;
    private final PaymentStatus status;
    private final String notes;

    public PaymentResponse(
            Long id,
            Long shootingId,
            String shootingTitle,
            String clientFirstName,
            String clientLastName,
            BigDecimal quoteTotalAmount,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentMethod method,
            PaymentType type,
            PaymentStatus status,
            String notes
    ) {
        this.id = id;
        this.shootingId = shootingId;
        this.shootingTitle = shootingTitle;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.quoteTotalAmount = quoteTotalAmount;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.type = type;
        this.status = status;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Long getShootingId() {
        return shootingId;
    }

    public String getShootingTitle() {
        return shootingTitle;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public BigDecimal getQuoteTotalAmount() {
        return quoteTotalAmount;
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
}