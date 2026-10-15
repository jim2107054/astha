package com.astha.ingest.controller;

import com.astha.ingest.service.IngestService;
import com.astha.ingest.service.IngestService.IngestResult;
import com.astha.platform.problem.ProblemDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/v1/events/transactions")
public class IngestController {

    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping
    public ResponseEntity<?> ingestTransaction(
            @RequestBody String rawPayload,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        long startTime = System.currentTimeMillis();
        IngestResult result = ingestService.ingestTransaction(rawPayload, idempotencyKey);
        long duration = System.currentTimeMillis() - startTime;

        if (!result.accepted()) {
            ProblemDetails problem = new ProblemDetails(
                    URI.create("urn:astha:error:schema-validation"),
                    "Transaction Schema Validation Error",
                    HttpStatus.BAD_REQUEST.value(),
                    result.errorMessage(),
                    URI.create("/v1/events/transactions")
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "ACCEPTED",
                "eventId", result.eventId(),
                "accountId", result.accountId(),
                "ingestTimestamp", result.timestamp().toString(),
                "durationMs", duration
        ));
    }
}
