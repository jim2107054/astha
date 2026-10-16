package com.astha.simulator.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ground_truth")
public class GroundTruthRecord {

    @Id
    private String recordId;
    private String entityOrEventId;
    private String typology; // MULE_CHAIN, YOUNG_BURST, QR_CASH_SUBSTITUTE, STRUCTURING
    private String ringId;
    private String label; // FRAUD, HARD_NEGATIVE, LEGITIMATE
    private Instant injectedAt;

    public GroundTruthRecord() {}

    public GroundTruthRecord(String recordId, String entityOrEventId, String typology,
                             String ringId, String label) {
        this.recordId = recordId;
        this.entityOrEventId = entityOrEventId;
        this.typology = typology;
        this.ringId = ringId;
        this.label = label;
        this.injectedAt = Instant.now();
    }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getEntityOrEventId() { return entityOrEventId; }
    public void setEntityOrEventId(String entityOrEventId) { this.entityOrEventId = entityOrEventId; }

    public String getTypology() { return typology; }
    public void setTypology(String typology) { this.typology = typology; }

    public String getRingId() { return ringId; }
    public void setRingId(String ringId) { this.ringId = ringId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getInjectedAt() { return injectedAt; }
    public void setInjectedAt(Instant injectedAt) { this.injectedAt = injectedAt; }
}
