package com.astha.risk.features;

import com.astha.risk.domain.TransactionScoreRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisFeatureStore implements FeatureStore {
    private static final Logger log = LoggerFactory.getLogger(RedisFeatureStore.class);

    private final StringRedisTemplate redisTemplate;
    private final Map<String, FeatureSnapshot> inMemoryStore = new ConcurrentHashMap<>();
    private volatile boolean healthy = true;

    @Autowired(required = false)
    public RedisFeatureStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisFeatureStore() {
        this.redisTemplate = null;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    @Override
    public boolean isHealthy() {
        if (!healthy) return false;
        if (redisTemplate == null) return true; // Running in embedded mode
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis ping failed, marking feature store unhealthy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public FeatureSnapshot getFeatures(TransactionScoreRequest request) {
        if (request.getFeatureOverrides() != null) {
            return request.getFeatureOverrides();
        }

        if (!isHealthy()) {
            // Degraded fallback returns baseline snapshot
            FeatureSnapshot fallback = inMemoryStore.getOrDefault(request.getAccountId(), new FeatureSnapshot());
            return fallback;
        }

        return inMemoryStore.computeIfAbsent(request.getAccountId(), k -> new FeatureSnapshot());
    }

    @Override
    public void updateFeatures(TransactionScoreRequest request) {
        FeatureSnapshot current = inMemoryStore.computeIfAbsent(request.getAccountId(), k -> new FeatureSnapshot());
        long amt = request.getAmountPaisa() != null ? request.getAmountPaisa() : 0L;
        current.setInflow5mPaisa(current.getInflow5mPaisa() + amt);
        current.setInflow1hPaisa(current.getInflow1hPaisa() + amt);
        current.setInflow24hPaisa(current.getInflow24hPaisa() + amt);
        current.setInflow7dPaisa(current.getInflow7dPaisa() + amt);
        if (request.getCounterpartyId() != null) {
            current.setDistinctSenders1h(current.getDistinctSenders1h() + 1);
        }
    }

    public void putInMemoryFeature(String accountId, FeatureSnapshot snapshot) {
        inMemoryStore.put(accountId, snapshot);
    }
}
