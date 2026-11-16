
package com.lensflow.api.shooting.entity;

import com.lensflow.api.client.entity.Client;
import com.lensflow.api.quote.entity.Quote;
import com.lensflow.api.shooting.entity.GalleryStatus;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shootings")
public class Shooting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(length = 250)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShootingStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", unique = true)
    private Quote quote;

    @Enumerated(EnumType.STRING)
    @Column(name = "gallery_status", nullable = false, length = 30)
    private GalleryStatus galleryStatus = GalleryStatus.NOT_STARTED;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime galleryStatusUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.lensflow.api.auth.entity.User owner;

    public com.lensflow.api.auth.entity.User getOwner() { return owner; }
    public void setOwner(com.lensflow.api.auth.entity.User owner) { this.owner = owner; }

    public Shooting() {
    }

    public Long getId() {
        return id;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Quote getQuote() {
    return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public GalleryStatus getGalleryStatus() {
        return galleryStatus;
    }

    public void setGalleryStatus(GalleryStatus galleryStatus) {
        if (this.galleryStatus != galleryStatus) {
            this.galleryStatus = galleryStatus;
            this.galleryStatusUpdatedAt = LocalDateTime.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;

        if (galleryStatusUpdatedAt == null) {
            galleryStatusUpdatedAt = now;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getGalleryStatusUpdatedAt() {
        return galleryStatusUpdatedAt;
    }
}
