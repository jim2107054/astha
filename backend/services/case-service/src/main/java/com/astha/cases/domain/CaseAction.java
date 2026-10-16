package com.astha.cases.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "case_actions")
public class CaseAction {

    @Id
    private String actionId;
    private String caseId;
    private String actorId;
    private String actionType; // COMMENT, ASSIGN, LABEL, FREEZE_RECOMMENDED, APPROVAL_REQUESTED, APPROVED, REJECTED, CLOSED
    private String note;
    private Instant timestamp;

    public CaseAction() {}

    public CaseAction(String actionId, String caseId, String actorId, String actionType, String note) {
        this.actionId = actionId;
        this.caseId = caseId;
        this.actorId = actorId;
        this.actionType = actionType;
        this.note = note;
        this.timestamp = Instant.now();
    }

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
