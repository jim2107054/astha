package com.astha.risk;

import com.astha.risk.domain.ScoreResponse;
import com.astha.risk.domain.TransactionScoreRequest;
import com.astha.risk.engine.RiskEngine;
import com.astha.risk.features.FeatureSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FeatureSnapshotReplayTest {

    @Autowired
    private RiskEngine riskEngine;

    @Test
    @DisplayName("RSK-1 & NFR-6: Golden-file replay of identical feature snapshot yields identical score and reasons")
    void shouldProduceDeterministicScoreOnReplay() {
        TransactionScoreRequest request1 = new TransactionScoreRequest();
        request1.setEventId("evt-golden-001");
        request1.setAccountId("acc-replay-100");
        request1.setAmountPaisa(2500000L);
        request1.setEventType("P2P");

        FeatureSnapshot snapshot = new FeatureSnapshot();
        snapshot.setDistinctSenders1h(6);
        snapshot.setInflow1hPaisa(3000000L);
        snapshot.setOutflowRatio30m(0.95);
        snapshot.setAccountAgeDays(10);
        request1.setFeatureOverrides(snapshot);

        ScoreResponse response1 = riskEngine.score(request1);

        // Replay with identical features under different event ID
        TransactionScoreRequest request2 = new TransactionScoreRequest();
        request2.setEventId("evt-golden-002");
        request2.setAccountId("acc-replay-100");
        request2.setAmountPaisa(2500000L);
        request2.setEventType("P2P");
        request2.setFeatureOverrides(snapshot);

        ScoreResponse response2 = riskEngine.score(request2);

        assertThat(response1.getScore()).isEqualTo(response2.getScore());
        assertThat(response1.getDecision()).isEqualTo(response2.getDecision());
        assertThat(response1.getReasonCodes())
                .extracting(ScoreResponse.ReasonCodeDetail::code)
                .containsExactlyElementsOf(
                        response2.getReasonCodes().stream().map(ScoreResponse.ReasonCodeDetail::code).toList()
                );
    }
}
