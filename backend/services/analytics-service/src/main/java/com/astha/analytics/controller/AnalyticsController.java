package com.astha.analytics.controller;

import com.astha.analytics.domain.RuleQualityStat;
import com.astha.analytics.service.AnalyticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<AnalyticsService.ExecutiveKpis> getKpis() {
        return ResponseEntity.ok(analyticsService.getExecutiveKpis());
    }

    @GetMapping("/rules-quality")
    public ResponseEntity<List<RuleQualityStat>> getRuleQuality() {
        return ResponseEntity.ok(analyticsService.getRuleQuality());
    }

    @PostMapping("/feedback")
    public ResponseEntity<RuleQualityStat> recordFeedback(@Valid @RequestBody FeedbackRequest request) {
        RuleQualityStat updated = analyticsService.recordFeedback(request.ruleId(), request.trueFraud());
        return ResponseEntity.ok(updated);
    }

    public record FeedbackRequest(
            @NotBlank String ruleId,
            @NotNull Boolean trueFraud
    ) {}
}
