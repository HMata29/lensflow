
package com.lensflow.api.shooting.dto;

import com.lensflow.api.shooting.entity.ShootingStatus;

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
            String clientLastName
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
}
