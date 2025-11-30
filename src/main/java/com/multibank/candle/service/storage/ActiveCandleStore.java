package com.multibank.candle.service.storage;

import com.multibank.candle.model.Candle;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ActiveCandleStore {
    
    Candle.Builder get(String key);
    
    Candle.Builder computeIfAbsent(String key, Function<String, Candle.Builder> mappingFunction);
    
    void remove(String key);
    
    void removeIf(Predicate<Map.Entry<String, Candle.Builder>> filter);
}

