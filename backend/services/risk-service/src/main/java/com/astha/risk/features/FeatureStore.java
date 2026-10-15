package com.astha.risk.features;

import com.astha.risk.domain.TransactionScoreRequest;

public interface FeatureStore {
    FeatureSnapshot getFeatures(TransactionScoreRequest request);
    void updateFeatures(TransactionScoreRequest request);
    boolean isHealthy();
}
