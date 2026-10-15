package com.astha.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rule_versions")
public class RuleVersion {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "rule_id", length = 64, nullable = false)
    private String ruleId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "status", length = 32, nullable = false)
    private String status; // DRAFT, PENDING_APPROVAL, ACTIVE, REJECTED, ARCHIVED

    @Column(name = "parameters_json", columnDefinition = "TEXT", nullable = false)
    private String parametersJson;

    @Column(name = "author_id", length = 64, nullable = false)
    private String authorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "approved_by", length = 64)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    public RuleVersion() {}

    public RuleVersion(String id, String ruleId, int versionNumber, String status, String parametersJson, String authorId) {
        this.id = id;
        this.ruleId = ruleId;
        this.versionNumber = versionNumber;
        this.status = status;
        this.parametersJson = parametersJson;
        this.authorId = authorId;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovalNotes() { return approvalNotes; }
    public void setApprovalNotes(String approvalNotes) { this.approvalNotes = approvalNotes; }
}
