package com.astha.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lists")
public class ListEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "list_type", length = 16, nullable = false)
    private String listType; // ALLOW, BLOCK

    @Column(name = "entity_type", length = 32, nullable = false)
    private String entityType; // ACCOUNT, DEVICE, PHONE, NID, AGENT, MERCHANT

    @Column(name = "entity_value", length = 128, nullable = false)
    private String entityValue;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "created_by", length = 64, nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    public ListEntity() {}

    public ListEntity(String listType, String entityType, String entityValue, String reason, String createdBy, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.listType = listType;
        this.entityType = entityType;
        this.entityValue = entityValue;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getListType() { return listType; }
    public void setListType(String listType) { this.listType = listType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityValue() { return entityValue; }
    public void setEntityValue(String entityValue) { this.entityValue = entityValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
