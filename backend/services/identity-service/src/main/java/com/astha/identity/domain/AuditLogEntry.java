package com.astha.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLogEntry {

    @Id
    private String logId;
    private String actorId;
    private String ipAddress;
    private String action; // e.g. REVEAL_PII, APPROVE_FREEZE, CHANGE_CONFIG
    private String resourceType;
    private String resourceId;
    private String detailsJson;
    private Instant timestamp;

    public AuditLogEntry() {}

    public AuditLogEntry(String logId, String actorId, String ipAddress, String action,
                         String resourceType, String resourceId, String detailsJson) {
        this.logId = logId;
        this.actorId = actorId;
        this.ipAddress = ipAddress;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.detailsJson = detailsJson;
        this.timestamp = Instant.now();
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
