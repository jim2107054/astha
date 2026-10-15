package com.astha.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rule_approvals")
public class RuleApproval {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "version_id", length = 64, nullable = false)
    private String versionId;

    @Column(name = "requested_by", length = 64, nullable = false)
    private String requestedBy;

    @Column(name = "decided_by", length = 64, nullable = false)
    private String decidedBy;

    @Column(name = "decision", length = 32, nullable = false)
    private String decision; // APPROVED, REJECTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    public RuleApproval() {}

    public RuleApproval(String versionId, String requestedBy, String decidedBy, String decision, String notes) {
        this.id = UUID.randomUUID();
        this.versionId = versionId;
        this.requestedBy = requestedBy;
        this.decidedBy = decidedBy;
        this.decision = decision;
        this.notes = notes;
        this.decidedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getVersionId() { return versionId; }
    public void setVersionId(String versionId) { this.versionId = versionId; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
