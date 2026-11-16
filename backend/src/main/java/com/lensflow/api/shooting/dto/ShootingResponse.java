
package com.lensflow.api.shooting.dto;

import com.lensflow.api.shooting.entity.ShootingStatus;
import com.lensflow.api.shooting.entity.GalleryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShootingResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final String location;
    private final ShootingStatus status;
    private final String notes;
    private final Long clientId;
    private final String clientFirstName;
    private final String clientLastName;
    private final Long quoteId;
    private final String quoteNumber;
    private final BigDecimal quoteTotalAmount;
    private final GalleryStatus galleryStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime galleryStatusUpdatedAt;

    public ShootingResponse(
            Long id,
            String title,
            String category,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String location,
            ShootingStatus status,
            String notes,
            Long clientId,
            String clientFirstName,
            String clientLastName,
            Long quoteId,
            String quoteNumber,
            BigDecimal quoteTotalAmount,
            GalleryStatus galleryStatus,
            LocalDateTime createdAt,
            LocalDateTime galleryStatusUpdatedAt
    ) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.startAt = startAt;
        this.endAt = endAt;
        this.location = location;
        this.status = status;
        this.notes = notes;
        this.clientId = clientId;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.quoteTotalAmount = quoteTotalAmount;
        this.galleryStatus = galleryStatus;
        this.createdAt = createdAt;
        this.galleryStatusUpdatedAt = galleryStatusUpdatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public String getLocation() {
        return location;
    }

    public ShootingStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
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

    public Long getQuoteId() {
        return quoteId;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public BigDecimal getQuoteTotalAmount() {
        return quoteTotalAmount;
    }

    public GalleryStatus getGalleryStatus() {
        return galleryStatus;
    }

    public LocalDateTime getCreatedAt() {
       return createdAt; 
    }

    public LocalDateTime getGalleryStatusUpdatedAt() {
        return galleryStatusUpdatedAt;
    }
}
