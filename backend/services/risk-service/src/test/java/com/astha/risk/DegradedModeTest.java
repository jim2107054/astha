package com.astha.risk;

import com.astha.risk.domain.ScoreResponse;
import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.engine.RiskEngine;
import com.astha.risk.features.RedisFeatureStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DegradedModeTest {

    @Autowired
    private RiskEngine riskEngine;

    @Autowired
    private RedisFeatureStore redisFeatureStore;

    @Test
    @DisplayName("RSK-6 & NFR-5: Safe fallback to ALLOW_WITH_FLAG when Redis/feature store is down")
    void shouldFallbackGracefullyWhenFeatureStoreIsDown() {
        try {
            // Simulate Redis failure
            redisFeatureStore.setHealthy(false);

            TransactionScoreRequest req = new TransactionScoreRequest();
            req.setEventId("evt-degraded-001");
            req.setAccountId("acc-degraded-999");
            req.setAmountPaisa(500000L);
            req.setEventType("P2P");

            ScoreResponse response = riskEngine.score(req);

            assertThat(response.isDegraded()).isTrue();
            assertThat(response.getDecision()).isEqualTo("ALLOW_WITH_FLAG");
            assertThat(response.getReasonCodes())
                    .extracting(ScoreResponse.ReasonCodeDetail::code)
                    .contains("DEGRADED_FEATURE_STORE_TIMEOUT");
        } finally {
            // Restore health
            redisFeatureStore.setHealthy(true);
        }
    }
}
