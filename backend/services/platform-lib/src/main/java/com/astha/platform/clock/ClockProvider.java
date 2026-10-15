package com.astha.platform.clock;

import java.time.Instant;
import java.time.ZoneId;

public interface ClockProvider {
    ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

    Instant now();

    default ZoneId getZoneId() {
        return DHAKA_ZONE;
    }

    static ClockProvider system() {
        return Instant::now;
    }
}
