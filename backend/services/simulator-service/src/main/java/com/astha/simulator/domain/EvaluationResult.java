package com.astha.simulator.domain;

import java.util.List;
import java.util.Map;

public record EvaluationResult(
        String configuration,
        long seed,
        double overallPrecision,
        double overallRecall,
        double overallF1,
        double falsePositiveRate,
        int totalAlerts,
        int totalCases,
        int analystMinutesSaved,
        DisputeSimSummary disputeSimulation,
        List<TypologyMetric> typologyMetrics
) {
    public record TypologyMetric(
            String typology,
            int groundTruthPositives,
            int truePositives,
            int falsePositives,
            double precision,
            double recall,
            double f1Score,
            long avgDetectionDelayMs,
            double alertsPerTrueFraud
    ) {}

    public record DisputeSimSummary(
            int totalDisputesSimulated,
            double slaCompliancePercentage,
            double autoRefundSharePercentage,
            double breachPredictionAccuracyPercentage,
            double meanTimeToResolveHours
    ) {}
}
