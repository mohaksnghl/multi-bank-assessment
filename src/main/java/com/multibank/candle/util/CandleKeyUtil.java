package com.multibank.candle.util;

import com.multibank.candle.model.Timeframe;

public class CandleKeyUtil {
    
    private static final String KEY_SEPARATOR = ":";
    
    public static String buildKey(String symbol, Timeframe timeframe, long timestamp) {
        return symbol + KEY_SEPARATOR + timeframe.getCode() + KEY_SEPARATOR + timestamp;
    }
    
    public static KeyInfo parseKey(String key) {
        String[] parts = key.split(KEY_SEPARATOR);
        if (parts.length != 3) {
            return null;
        }
        
        Timeframe timeframe = Timeframe.fromCode(parts[1]);
        if (timeframe == null) {
            return null;
        }
        
        return new KeyInfo(parts[0], timeframe);
    }
    
    public record KeyInfo(String symbol, Timeframe timeframe) {}
}

