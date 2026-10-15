package com.astha.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "typology", length = 64, nullable = false)
    private String typology;

    @Column(name = "severity", length = 32, nullable = false)
    private String severity;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "active_version_id", length = 64)
    private String activeVersionId;

    @Column(name = "description_en", columnDefinition = "TEXT", nullable = false)
    private String descriptionEn;

    @Column(name = "description_bn", columnDefinition = "TEXT", nullable = false)
    private String descriptionBn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Rule() {}

    public Rule(String id, String name, String typology, String severity, int weight, String activeVersionId, String descriptionEn, String descriptionBn) {
        this.id = id;
        this.name = name;
        this.typology = typology;
        this.severity = severity;
        this.weight = weight;
        this.activeVersionId = activeVersionId;
        this.descriptionEn = descriptionEn;
        this.descriptionBn = descriptionBn;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTypology() { return typology; }
    public void setTypology(String typology) { this.typology = typology; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getActiveVersionId() { return activeVersionId; }
    public void setActiveVersionId(String activeVersionId) { this.activeVersionId = activeVersionId; }

    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }

    public String getDescriptionBn() { return descriptionBn; }
    public void setDescriptionBn(String descriptionBn) { this.descriptionBn = descriptionBn; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
