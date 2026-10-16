package com.astha.graph.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "graph_nodes")
public class GraphNode {

    @Id
    private String id;
    private String nodeType; // ACCOUNT, DEVICE, PHONE, AGENT, MERCHANT
    private String label;
    private int riskScore;
    private Instant firstSeen;
    private Instant lastSeen;

    public GraphNode() {}

    public GraphNode(String id, String nodeType, String label, int riskScore) {
        this.id = id;
        this.nodeType = nodeType;
        this.label = label;
        this.riskScore = riskScore;
        this.firstSeen = Instant.now();
        this.lastSeen = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}
