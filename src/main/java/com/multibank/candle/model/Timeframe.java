package com.multibank.candle.model;

import lombok.Getter;

import java.time.Duration;
import java.util.Arrays;

public enum Timeframe {
    ONE_SECOND("1s", Duration.ofSeconds(1)),
    FIVE_SECONDS("5s", Duration.ofSeconds(5)),
    ONE_MINUTE("1m", Duration.ofMinutes(1)),
    FIFTEEN_MINUTES("15m", Duration.ofMinutes(15)),
    ONE_HOUR("1h", Duration.ofHours(1));
    
    @Getter
    private final String code;
    private final Duration duration;
    
    Timeframe(String code, Duration duration) {
        this.code = code;
        this.duration = duration;
    }

    public long getSeconds() {
        return duration.getSeconds();
    }
    
    public static Timeframe fromCode(String code) {
        return Arrays.stream(values())
            .filter(tf -> tf.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Calculates the start timestamp of the candle window that contains the given timestamp.
     * Rounds down the timestamp to the nearest window boundary.
     * 
     * @param timestamp The timestamp to align (Unix timestamp in seconds)
     * @return The start timestamp of the candle window containing the given timestamp
     * 
     * @example
     * For 1-minute timeframe:
     * getWindowStartTimestamp(1620000045) returns 1620000000 (start of minute)
     * getWindowStartTimestamp(1620000059) returns 1620000000 (start of same minute)
     * getWindowStartTimestamp(1620000060) returns 1620000060 (start of next minute)
     * 
     * @example
     * For 5-second timeframe:
     * getWindowStartTimestamp(1620000007) returns 1620000005 (start of 5-second window)
     * getWindowStartTimestamp(1620000009) returns 1620000005 (start of same window)
     * getWindowStartTimestamp(1620000010) returns 1620000010 (start of next window)
     */
    public long getWindowStartTimestamp(long timestamp) {
        return (timestamp / getSeconds()) * getSeconds();
    }
}

