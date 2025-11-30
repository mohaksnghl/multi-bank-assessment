package com.multibank.candle.repository;

import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Primary
public class InMemoryCandleRepository implements CandleRepository {
    
    private final ConcurrentHashMap<String, NavigableMap<Long, Candle>> storage = new ConcurrentHashMap<>();
    
    @Override
    public void save(String symbol, Timeframe timeframe, Candle candle) {
        String key = symbol + ":" + timeframe.getCode();
        
        NavigableMap<Long, Candle> candles = storage.computeIfAbsent(
            key, 
            k -> new java.util.concurrent.ConcurrentSkipListMap<>()
        );
        
        candles.put(candle.getTime(), candle);
    }
    
    @Override
    public List<Candle> findBySymbolAndInterval(String symbol, Timeframe timeframe, long from, long to) {
        String key = symbol + ":" + timeframe.getCode();
        NavigableMap<Long, Candle> candles = storage.get(key);
        
        if (candles == null || candles.isEmpty()) {
            return new ArrayList<>();
        }
        
        NavigableMap<Long, Candle> subMap = candles.subMap(from, true, to, false);
        return new ArrayList<>(subMap.values());
    }
}

