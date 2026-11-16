package com.lensflow.api.quote.dto;

import com.lensflow.api.quote.entity.QuoteStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class QuoteRequest {

    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(max = 150, message = "Il titolo non può superare 150 caratteri")
    private String title;

    @Size(max = 2000, message = "Le note non possono superare 2000 caratteri")
    private String notes;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @NotNull(message = "Lo stato è obbligatorio")
    private QuoteStatus status;

    @NotNull(message = "Il cliente è obbligatorio")
    private Long clientId;

    @Valid
    private List<QuoteItemRequest> items;

    public QuoteRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<QuoteItemRequest> getItems() {
        return items;
    }

    public void setItems(List<QuoteItemRequest> items) {
        this.items = items;
    }
}