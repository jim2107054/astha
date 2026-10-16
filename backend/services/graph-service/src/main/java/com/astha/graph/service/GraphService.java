package com.astha.graph.service;

import com.astha.graph.domain.FraudRing;
import com.astha.graph.domain.GraphEdge;
import com.astha.graph.domain.GraphNode;
import com.astha.graph.repository.FraudRingRepository;
import com.astha.graph.repository.GraphEdgeRepository;
import com.astha.graph.repository.GraphNodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphService {

    private static final Logger log = LoggerFactory.getLogger(GraphService.class);

    private final GraphNodeRepository nodeRepository;
    private final GraphEdgeRepository edgeRepository;
    private final FraudRingRepository ringRepository;
    private final ObjectMapper objectMapper;

    public GraphService(GraphNodeRepository nodeRepository,
                        GraphEdgeRepository edgeRepository,
                        FraudRingRepository ringRepository,
                        ObjectMapper objectMapper) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.ringRepository = ringRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GraphNode getOrCreateNode(String id, String type, String label, int riskScore) {
        return nodeRepository.findById(id).orElseGet(() -> {
            GraphNode n = new GraphNode(id, type, label, riskScore);
            return nodeRepository.save(n);
        });
    }

    @Transactional
    public GraphEdge recordEdge(String sourceId, String targetId, String relationship, Long amountPaisa) {
        getOrCreateNode(sourceId, "ACCOUNT", sourceId, 0);
        getOrCreateNode(targetId, "ACCOUNT", targetId, 0);

        String edgeId = sourceId + "->" + relationship + "->" + targetId;
        GraphEdge edge = edgeRepository.findById(edgeId).orElseGet(() ->
                new GraphEdge(edgeId, sourceId, targetId, relationship, 0L, 0)
        );

        edge.setWeightAmountPaisa(edge.getWeightAmountPaisa() + (amountPaisa != null ? amountPaisa : 0L));
        edge.setTransactionCount(edge.getTransactionCount() + 1);
        edge.setLastTimestamp(Instant.now());
        return edgeRepository.save(edge);
    }

    public Subgraph getNeighbors(String startNodeId, int maxDepth) {
        int depthLimit = Math.min(Math.max(1, maxDepth), 3);

        Set<String> visitedNodeIds = new HashSet<>();
        Set<GraphEdge> collectedEdges = new HashSet<>();
        Queue<String> currentLevel = new LinkedList<>();

        currentLevel.add(startNodeId);
        visitedNodeIds.add(startNodeId);

        for (int depth = 0; depth < depthLimit; depth++) {
            int levelSize = currentLevel.size();
            if (levelSize == 0) break;

            Queue<String> nextLevel = new LinkedList<>();
            for (int i = 0; i < levelSize; i++) {
                String currentId = currentLevel.poll();
                List<GraphEdge> edges = edgeRepository.findBySourceNodeIdOrTargetNodeId(currentId, currentId);
                for (GraphEdge edge : edges) {
                    collectedEdges.add(edge);
                    String neighborId = edge.getSourceNodeId().equals(currentId) ? edge.getTargetNodeId() : edge.getSourceNodeId();
                    if (!visitedNodeIds.contains(neighborId)) {
                        visitedNodeIds.add(neighborId);
                        nextLevel.add(neighborId);
                    }
                }
            }
            currentLevel = nextLevel;
        }

        List<GraphNode> nodes = nodeRepository.findAllById(visitedNodeIds);
        return new Subgraph(startNodeId, depthLimit, nodes, new ArrayList<>(collectedEdges));
    }

    @Transactional
    public List<FraudRing> detectMuleRings() {
        List<FraudRing> detectedRings = new ArrayList<>();
        List<GraphEdge> allEdges = edgeRepository.findAll();

        // 1. Group edges by target node to detect Fan-in (Aggregation)
        Map<String, List<GraphEdge>> inEdgesByTarget = allEdges.stream()
                .filter(e -> "TRANSFERRED_TO".equals(e.getRelationship()))
                .collect(Collectors.groupingBy(GraphEdge::getTargetNodeId));

        // Group edges by source node to detect Fan-out (Dispersal)
        Map<String, List<GraphEdge>> outEdgesBySource = allEdges.stream()
                .filter(e -> "TRANSFERRED_TO".equals(e.getRelationship()))
                .collect(Collectors.groupingBy(GraphEdge::getSourceNodeId));

        for (Map.Entry<String, List<GraphEdge>> entry : inEdgesByTarget.entrySet()) {
            String candidateMule = entry.getKey();
            List<GraphEdge> inEdges = entry.getValue();

            // Distinct depositors >= 3
            Set<String> depositors = inEdges.stream().map(GraphEdge::getSourceNodeId).collect(Collectors.toSet());
            if (depositors.size() >= 3) {
                List<GraphEdge> outEdges = outEdgesBySource.getOrDefault(candidateMule, Collections.emptyList());
                long totalInPaisa = inEdges.stream().mapToLong(GraphEdge::getWeightAmountPaisa).sum();
                long totalOutPaisa = outEdges.stream().mapToLong(GraphEdge::getWeightAmountPaisa).sum();

                // If outflow >= 70% of inflow or high fan-out -> Pass-through Mule Chain
                if (outEdges.size() >= 1 && totalOutPaisa >= (totalInPaisa * 0.70)) {
                    String ringId = "RING-" + UUID.randomUUID().toString().substring(0, 8);
                    Set<String> allMembers = new HashSet<>(depositors);
                    allMembers.add(candidateMule);
                    outEdges.forEach(e -> allMembers.add(e.getTargetNodeId()));

                    Map<String, Object> evidence = Map.of(
                            "muleNodeId", candidateMule,
                            "depositorCount", depositors.size(),
                            "totalInPaisa", totalInPaisa,
                            "totalOutPaisa", totalOutPaisa,
                            "inEdges", inEdges.stream().map(GraphEdge::getId).toList(),
                            "outEdges", outEdges.stream().map(GraphEdge::getId).toList()
                    );

                    String evidenceJson;
                    try {
                        evidenceJson = objectMapper.writeValueAsString(evidence);
                    } catch (Exception e) {
                        evidenceJson = "{}";
                    }

                    FraudRing ring = new FraudRing(
                            ringId,
                            "FAN_IN_FAN_OUT",
                            88, // high risk score
                            "DETECTED",
                            allMembers.size(),
                            evidenceJson
                    );
                    detectedRings.add(ringRepository.save(ring));
                }
            }
        }

        // 2. Detect Shared-Device Clusters
        Map<String, List<GraphEdge>> edgesByDevice = allEdges.stream()
                .filter(e -> "SHARED_DEVICE".equals(e.getRelationship()))
                .collect(Collectors.groupingBy(GraphEdge::getTargetNodeId));

        for (Map.Entry<String, List<GraphEdge>> entry : edgesByDevice.entrySet()) {
            String deviceId = entry.getKey();
            Set<String> linkedAccounts = entry.getValue().stream().map(GraphEdge::getSourceNodeId).collect(Collectors.toSet());
            if (linkedAccounts.size() >= 3) {
                String ringId = "RING-DEV-" + UUID.randomUUID().toString().substring(0, 8);
                Map<String, Object> evidence = Map.of(
                        "deviceId", deviceId,
                        "accountCount", linkedAccounts.size(),
                        "accounts", linkedAccounts
                );
                String evidenceJson;
                try {
                    evidenceJson = objectMapper.writeValueAsString(evidence);
                } catch (Exception e) {
                    evidenceJson = "{}";
                }

                FraudRing ring = new FraudRing(
                        ringId,
                        "SHARED_DEVICE_CLUSTER",
                        92,
                        "DETECTED",
                        linkedAccounts.size() + 1,
                        evidenceJson
                );
                detectedRings.add(ringRepository.save(ring));
            }
        }

        return detectedRings;
    }

    public List<FraudRing> getAllRings() {
        return ringRepository.findAll();
    }

    public Optional<FraudRing> getRing(String ringId) {
        return ringRepository.findById(ringId);
    }

    public record Subgraph(String rootNodeId, int depth, List<GraphNode> nodes, List<GraphEdge> edges) {}
}
