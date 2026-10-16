package com.astha.graph.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "graph_edges")
public class GraphEdge {

    @Id
    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private String relationship; // TRANSFERRED_TO, SHARED_DEVICE, SHARED_PHONE, AGENT_SERVED
    private Long weightAmountPaisa;
    private int transactionCount;
    private Instant firstTimestamp;
    private Instant lastTimestamp;

    public GraphEdge() {}

    public GraphEdge(String id, String sourceNodeId, String targetNodeId, String relationship,
                     Long weightAmountPaisa, int transactionCount) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.relationship = relationship;
        this.weightAmountPaisa = weightAmountPaisa;
        this.transactionCount = transactionCount;
        this.firstTimestamp = Instant.now();
        this.lastTimestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Long getWeightAmountPaisa() { return weightAmountPaisa; }
    public void setWeightAmountPaisa(Long weightAmountPaisa) { this.weightAmountPaisa = weightAmountPaisa; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public Instant getFirstTimestamp() { return firstTimestamp; }
    public void setFirstTimestamp(Instant firstTimestamp) { this.firstTimestamp = firstTimestamp; }

    public Instant getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(Instant lastTimestamp) { this.lastTimestamp = lastTimestamp; }
}
