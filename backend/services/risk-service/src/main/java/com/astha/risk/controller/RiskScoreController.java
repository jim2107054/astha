package com.astha.risk.controller;

import com.astha.risk.domain.ScoreResponse;
import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.engine.RiskEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/risk")
public class RiskScoreController {

    private final RiskEngine riskEngine;

    public RiskScoreController(RiskEngine riskEngine) {
        this.riskEngine = riskEngine;
    }

    @PostMapping("/score")
    public ResponseEntity<ScoreResponse> scoreTransaction(@Valid @RequestBody TransactionScoreRequest request) {
        ScoreResponse response = riskEngine.score(request);
        return ResponseEntity.ok(response);
    }
}
