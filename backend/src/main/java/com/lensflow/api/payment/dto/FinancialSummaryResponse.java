
package com.lensflow.api.payment.dto;

import com.lensflow.api.payment.entity.FinancialStatus;

import java.math.BigDecimal;

public class FinancialSummaryResponse {

    private final Long shootingId;
    private final String shootingTitle;
    private final Long quoteId;
    private final String quoteNumber;
    private final BigDecimal totalAmount;
    private final BigDecimal paidAmount;
    private final BigDecimal pendingAmount;
    private final BigDecimal remainingAmount;
    private final BigDecimal paidPercentage;
    private final FinancialStatus status;

    public FinancialSummaryResponse(
            Long shootingId,
            String shootingTitle,
            Long quoteId,
            String quoteNumber,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal pendingAmount,
            BigDecimal remainingAmount,
            BigDecimal paidPercentage,
            FinancialStatus status
    ) {
        this.shootingId = shootingId;
        this.shootingTitle = shootingTitle;
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.remainingAmount = remainingAmount;
        this.paidPercentage = paidPercentage;
        this.status = status;
    }

    public Long getShootingId() {
        return shootingId;
    }

    public String getShootingTitle() {
        return shootingTitle;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public BigDecimal getPaidPercentage() {
        return paidPercentage;
    }

    public FinancialStatus getStatus() {
        return status;
    }
}
