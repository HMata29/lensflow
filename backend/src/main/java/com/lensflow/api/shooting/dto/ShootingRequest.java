
package com.lensflow.api.shooting.dto;

import com.lensflow.api.shooting.entity.ShootingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ShootingRequest {

    @NotBlank(message = "Il titolo è obbligatorio")
    @Size(max = 150, message = "Il titolo non può superare 150 caratteri")
    private String title;

    @NotBlank(message = "La categoria è obbligatoria")
    @Size(max = 100, message = "La categoria non può superare 100 caratteri")
    private String category;

    @NotNull(message = "La data di inizio è obbligatoria")
    private LocalDateTime startAt;

    @NotNull(message = "La data di fine è obbligatoria")
    private LocalDateTime endAt;

    @Size(max = 250, message = "La location non può superare 250 caratteri")
    private String location;

    @NotNull(message = "Lo stato è obbligatorio")
    private ShootingStatus status;

    private String notes;

    @NotNull(message = "Il cliente è obbligatorio")
    private Long clientId;

    @Positive(message = "L'ID del preventivo deve essere positivo")
    private Long quoteId;

    public ShootingRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ShootingStatus getStatus() {
        return status;
    }

    public void setStatus(ShootingStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }
}
