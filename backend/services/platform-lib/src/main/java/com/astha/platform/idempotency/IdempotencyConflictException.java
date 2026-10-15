package com.astha.platform.idempotency;

public class IdempotencyConflictException extends RuntimeException {
    private final String key;

    public IdempotencyConflictException(String key, String message) {
        super(message);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
