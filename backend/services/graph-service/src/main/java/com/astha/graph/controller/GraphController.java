package com.astha.graph.controller;

import com.astha.graph.domain.FraudRing;
import com.astha.graph.domain.GraphEdge;
import com.astha.graph.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/entities/{id}/neighbors")
    public ResponseEntity<GraphService.Subgraph> getNeighbors(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int depth) {
        return ResponseEntity.ok(graphService.getNeighbors(id, depth));
    }

    @PostMapping("/edges")
    public ResponseEntity<GraphEdge> recordEdge(@RequestBody Map<String, Object> req) {
        String sourceId = (String) req.get("sourceId");
        String targetId = (String) req.get("targetId");
        String rel = (String) req.getOrDefault("relationship", "TRANSFERRED_TO");
        Long amount = req.get("amountPaisa") instanceof Number n ? n.longValue() : 0L;

        return ResponseEntity.ok(graphService.recordEdge(sourceId, targetId, rel, amount));
    }

    @PostMapping("/jobs/detect-rings")
    public ResponseEntity<List<FraudRing>> triggerRingDetection() {
        return ResponseEntity.ok(graphService.detectMuleRings());
    }

    @GetMapping("/rings")
    public ResponseEntity<List<FraudRing>> getAllRings() {
        return ResponseEntity.ok(graphService.getAllRings());
    }

    @GetMapping("/rings/{id}")
    public ResponseEntity<FraudRing> getRingById(@PathVariable String id) {
        return graphService.getRing(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
