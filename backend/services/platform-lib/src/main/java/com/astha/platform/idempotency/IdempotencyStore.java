package com.astha.platform.idempotency;

import java.util.Optional;

public interface IdempotencyStore {
    Optional<IdempotencyRecord> findByKey(String key);
    void save(IdempotencyRecord record);
}
