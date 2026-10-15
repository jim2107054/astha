package com.astha.risk;

import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.features.FeatureSnapshot;
import com.astha.risk.rules.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleCatalogTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("R01: Pass-through mule detects high distinct senders followed by rapid outflow")
    void shouldHitR01PassThroughMule() {
        R01PassThroughMuleRule rule = new R01PassThroughMuleRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setAmountPaisa(1500000L);

        FeatureSnapshot features = new FeatureSnapshot();
        features.setDistinctSenders1h(5);
        features.setInflow1hPaisa(2000000L);
        features.setOutflowRatio30m(0.92);
        features.setAccountAgeDays(14);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R01_FAST_PASS_THROUGH");
        assertThat(result.textBn()).contains("পাস-থ্রু");
    }

    @Test
    @DisplayName("R02: Young account burst detects accounts < 7 days receiving large funds")
    void shouldHitR02YoungAccountBurst() {
        R02YoungAccountBurstRule rule = new R02YoungAccountBurstRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setAmountPaisa(3000000L);

        FeatureSnapshot features = new FeatureSnapshot();
        features.setAccountAgeDays(3);
        features.setInflow24hPaisa(3000000L);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R02_YOUNG_ACCOUNT_BURST");
    }

    @Test
    @DisplayName("R03: QR cash substitute detects repetitive round amounts on merchant QR")
    void shouldHitR03QrCashSubstitute() {
        R03QrCashSubstituteRule rule = new R03QrCashSubstituteRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setEventType("MERCHANT_QR");

        FeatureSnapshot features = new FeatureSnapshot();
        features.setRoundAmountRatio(0.85);
        features.setPayerRepeatRate(0.40);
        features.setNearLimitRatio(0.70);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R03_BANGLA_QR_CASH_SUBSTITUTE");
    }

    @Test
    @DisplayName("R04: Account takeover detects drain within 24h of SIM/device change")
    void shouldHitR04AccountTakeover() {
        R04AccountTakeoverRule rule = new R04AccountTakeoverRule();
        TransactionScoreRequest req = new TransactionScoreRequest();

        FeatureSnapshot features = new FeatureSnapshot();
        features.setHoursSinceSimOrDeviceChange(4.5);
        features.setBalanceDrainRatio(0.95);
        features.setNewBeneficiary(true);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R04_ACCOUNT_TAKEOVER_DRAIN");
    }

    @Test
    @DisplayName("R05: Structuring detects multiple near-limit transactions")
    void shouldHitR05Structuring() {
        R05StructuringRule rule = new R05StructuringRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setAmountPaisa(2450000L); // near 25,000 Taka limit

        FeatureSnapshot features = new FeatureSnapshot();
        features.setNearLimitCount24h(2);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R05_STRUCTURING_NEAR_LIMIT");
    }

    @Test
    @DisplayName("R06: Agent anomaly detects outlier velocity")
    void shouldHitR06AgentAnomaly() {
        R06AgentAnomalyRule rule = new R06AgentAnomalyRule();
        TransactionScoreRequest req = new TransactionScoreRequest();

        FeatureSnapshot features = new FeatureSnapshot();
        features.setAgentCashoutVelocityZScore(3.8);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R06_AGENT_VELOCITY_ANOMALY");
    }

    @Test
    @DisplayName("R07: Dormant account wake-up detects sudden high-value transfer")
    void shouldHitR07DormantWakeUp() {
        R07DormantWakeUpRule rule = new R07DormantWakeUpRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setAmountPaisa(2500000L);

        FeatureSnapshot features = new FeatureSnapshot();
        features.setIdleDays(120);
        features.setNewDevice(true);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R07_DORMANT_ACCOUNT_SUDDEN_ACTIVITY");
    }

    @Test
    @DisplayName("R08: Shared device or blocklisted entity flags high severity hit")
    void shouldHitR08SharedDeviceListed() {
        R08SharedDeviceListedRule rule = new R08SharedDeviceListedRule();
        TransactionScoreRequest req = new TransactionScoreRequest();

        FeatureSnapshot features = new FeatureSnapshot();
        features.setBlocklisted(true);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R08_BLOCKLISTED_OR_RING_ASSOCIATION");
    }

    @Test
    @DisplayName("R09: Ring score detects graph cluster detection")
    void shouldHitR09RingScore() {
        R09RingScoreRule rule = new R09RingScoreRule();
        TransactionScoreRequest req = new TransactionScoreRequest();

        FeatureSnapshot features = new FeatureSnapshot();
        features.setRingScore(88);
        features.setRingSize(5);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R09_GRAPH_MULE_RING_DETECTED");
    }

    @Test
    @DisplayName("R10: Failed-debit watch detects pending/failed transactions for dispute triage")
    void shouldHitR10FailedDebitWatch() {
        R10FailedDebitWatchRule rule = new R10FailedDebitWatchRule();
        TransactionScoreRequest req = new TransactionScoreRequest();
        req.setStatus("FAILED");

        FeatureSnapshot features = new FeatureSnapshot();
        features.setAgeMinutes(15);

        RuleEvaluationResult result = rule.evaluate(req, features, mapper.createObjectNode());
        assertThat(result.hit()).isTrue();
        assertThat(result.reasonCode()).isEqualTo("R10_DEBITED_BUT_FAILED_WATCH");
    }
}
