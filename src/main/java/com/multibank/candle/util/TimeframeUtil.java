package com.multibank.candle.util;

import com.multibank.candle.model.Timeframe;

public class TimeframeUtil {
    
    public static Timeframe parseInterval(String interval) {
        if (interval == null || interval.trim().isEmpty()) {
            throw new IllegalArgumentException("Interval cannot be null or empty");
        }
        
        Timeframe timeframe = Timeframe.fromCode(interval.trim());
        if (timeframe == null) {
            throw new IllegalArgumentException(
                "Invalid interval: " + interval + ". Supported: 1s, 5s, 1m, 15m, 1h");
        }
        
        return timeframe;
    }
}

