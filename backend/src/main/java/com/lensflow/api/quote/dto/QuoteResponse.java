
package com.lensflow.api.quote.dto;

import com.lensflow.api.quote.entity.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class QuoteResponse {

    private final Long id;
    private final String quoteNumber;
    private final String title;
    private final String notes;
    private final LocalDate issueDate;
    private final LocalDate expiryDate;
    private final QuoteStatus status;
    private final Long clientId;
    private final String clientFirstName;
    private final String clientLastName;
    private final List<QuoteItemResponse> items;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime acceptedAt;

    public QuoteResponse(
            Long id,
            String quoteNumber,
            String title,
            String notes,
            LocalDate issueDate,
            LocalDate expiryDate,
            QuoteStatus status,
            Long clientId,
            String clientFirstName,
            String clientLastName,
            List<QuoteItemResponse> items,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime acceptedAt
            
    ) {
        this.id = id;
        this.quoteNumber = quoteNumber;
        this.title = title;
        this.notes = notes;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.clientId = clientId;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.acceptedAt = acceptedAt;
    }

    public Long getId() {
        return id;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public List<QuoteItemResponse> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
}
