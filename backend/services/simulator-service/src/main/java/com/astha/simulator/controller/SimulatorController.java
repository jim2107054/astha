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
    private final com.astha.simulator.service.EvaluationRunnerService evaluationRunnerService;

    public SimulatorController(SimulatorService simulatorService,
                               com.astha.simulator.service.EvaluationRunnerService evaluationRunnerService) {
        this.simulatorService = simulatorService;
        this.evaluationRunnerService = evaluationRunnerService;
    }

    @PostMapping("/reset")
    public ResponseEntity<List<SyntheticEntity>> resetWithSeed(@RequestBody Map<String, Object> req) {
        long seed = req.containsKey("seed") ? ((Number) req.get("seed")).longValue() : 42L;
        return ResponseEntity.ok(simulatorService.resetWithSeed(seed));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<com.astha.simulator.domain.EvaluationResult> evaluate(
            @RequestBody(required = false) Map<String, Object> req) {
        String config = req != null && req.containsKey("config") ? (String) req.get("config") : "CONFIG_C_PLUS_GROUPING";
        long seed = req != null && req.containsKey("seed") ? ((Number) req.get("seed")).longValue() : 42L;
        return ResponseEntity.ok(evaluationRunnerService.runEvaluation(config, seed));
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
