package com.astha.analytics.service;

import com.astha.analytics.domain.RuleQualityStat;
import com.astha.analytics.repository.RuleQualityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyticsService {

    private final RuleQualityRepository qualityRepository;

    public AnalyticsService(RuleQualityRepository qualityRepository) {
        this.qualityRepository = qualityRepository;
        seedRuleQualityBaselines();
    }

    private void seedRuleQualityBaselines() {
        if (qualityRepository.count() == 0) {
            qualityRepository.save(new RuleQualityStat("R01_PASS_THROUGH_MULE", "Rapid Pass-through Movement", 120, 112, 8));
            qualityRepository.save(new RuleQualityStat("R02_YOUNG_ACCOUNT_BURST", "Young Account Velocity Burst", 85, 78, 7));
            qualityRepository.save(new RuleQualityStat("R03_QR_CASH_SUBSTITUTE", "Bangla QR Cash Substitute", 60, 52, 8));
            qualityRepository.save(new RuleQualityStat("R04_ACCOUNT_TAKEOVER", "Account Takeover after SIM/Device Change", 45, 43, 2));
            qualityRepository.save(new RuleQualityStat("R05_STRUCTURING_NEAR_LIMIT", "Structuring Near Regulatory Limit", 95, 88, 7));
            qualityRepository.save(new RuleQualityStat("R06_AGENT_ANOMALY", "Agent Outlier Velocity Anomaly", 35, 31, 4));
            qualityRepository.save(new RuleQualityStat("R07_DORMANT_WAKE_UP", "Dormant Account High-Value Transfer", 40, 36, 4));
            qualityRepository.save(new RuleQualityStat("R08_SHARED_DEVICE_LISTED", "Shared Blocklisted Device Identifier", 110, 107, 3));
            qualityRepository.save(new RuleQualityStat("R09_RING_SCORE", "Graph Mule Ring High Density", 28, 27, 1));
            qualityRepository.save(new RuleQualityStat("R10_FAILED_DEBIT_WATCH", "Failed Debit Watch for Auto-Refund", 150, 146, 4));
        }
    }

    public ExecutiveKpis getExecutiveKpis() {
        return new ExecutiveKpis(
                45800000L, // 4,58,000 Taka prevented loss
                12450L,    // total decisions
                148,       // alerts total
                18,        // active cases
                96.8,      // SLA compliance rate percentage
                14.5,      // MTTR in hours
                42.0       // Auto-refund percentage share
        );
    }

    public List<RuleQualityStat> getRuleQuality() {
        return qualityRepository.findAll();
    }

    @Transactional
    public RuleQualityStat recordFeedback(String ruleId, boolean isTrueFraud) {
        RuleQualityStat stat = qualityRepository.findById(ruleId).orElseGet(() ->
                new RuleQualityStat(ruleId, ruleId, 0, 0, 0)
        );

        stat.setTotalTriggerCount(stat.getTotalTriggerCount() + 1);
        if (isTrueFraud) {
            stat.setTrueFraudCount(stat.getTrueFraudCount() + 1);
        } else {
            stat.setFalsePositiveCount(stat.getFalsePositiveCount() + 1);
        }
        stat.setPrecisionRate((double) stat.getTrueFraudCount() / stat.getTotalTriggerCount());
        stat.setFalsePositiveRate((double) stat.getFalsePositiveCount() / stat.getTotalTriggerCount());

        return qualityRepository.save(stat);
    }

    public record ExecutiveKpis(
            Long totalLossPreventedPaisa,
            Long totalDecisionsCount,
            int alertsTotal,
            int activeCasesCount,
            double slaComplianceRate,
            double meanTimeToResolveHours,
            double autoRefundSharePercentage
    ) {}
}
