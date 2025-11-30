package com.multibank.candle.service.storage;

import com.multibank.candle.model.Candle;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@Primary
public class InMemoryActiveCandleStore implements ActiveCandleStore {
    
    private final ConcurrentHashMap<String, Candle.Builder> store = new ConcurrentHashMap<>();
    
    @Override
    public Candle.Builder get(String key) {
        return store.get(key);
    }
    
    @Override
    public Candle.Builder computeIfAbsent(String key, Function<String, Candle.Builder> mappingFunction) {
        return store.computeIfAbsent(key, mappingFunction);
    }
    
    @Override
    public void remove(String key) {
        store.remove(key);
    }
    
    @Override
    public void removeIf(java.util.function.Predicate<Map.Entry<String, Candle.Builder>> filter) {
        store.entrySet().removeIf(filter);
    }

}

