package com.astha.simulator.controller;

import com.astha.simulator.domain.GroundTruthRecord;
import com.astha.simulator.domain.SyntheticEntity;
import com.astha.simulator.service.SimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/simulator")
public class SimulatorController {

    private final SimulatorService simulatorService;

    public SimulatorController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @PostMapping("/reset")
    public ResponseEntity<List<SyntheticEntity>> resetWithSeed(@RequestBody Map<String, Object> req) {
        long seed = req.containsKey("seed") ? ((Number) req.get("seed")).longValue() : 42L;
        return ResponseEntity.ok(simulatorService.resetWithSeed(seed));
    }

    @PostMapping("/scenarios/inject")
    public ResponseEntity<GroundTruthRecord> injectScenario(@RequestBody Map<String, String> req) {
        String typology = req.getOrDefault("typology", "MULE_CHAIN");
        String targetEntityId = req.get("targetEntityId");
        return ResponseEntity.ok(simulatorService.injectTypology(typology, targetEntityId));
    }

    @GetMapping("/ground-truth")
    public ResponseEntity<List<GroundTruthRecord>> getGroundTruth() {
        return ResponseEntity.ok(simulatorService.getGroundTruth());
    }

    @GetMapping("/population")
    public ResponseEntity<List<SyntheticEntity>> getPopulation() {
        return ResponseEntity.ok(simulatorService.getPopulation());
    }
}
