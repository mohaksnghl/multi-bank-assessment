package com.multibank.candle.service;

import com.multibank.candle.exception.InvalidEventException;
import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import com.multibank.candle.repository.CandleRepository;
import com.multibank.candle.repository.InMemoryCandleRepository;
import com.multibank.candle.service.storage.ActiveCandleStore;
import com.multibank.candle.service.storage.InMemoryActiveCandleStore;
import com.multibank.candle.service.storage.InMemoryLockManager;
import com.multibank.candle.service.storage.LockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandleAggregationServiceTest {
    
    private CandleRepository repository;
    private CandleAggregationService service;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryCandleRepository();
        ActiveCandleStore activeCandleStore = new InMemoryActiveCandleStore();
        LockManager lockManager = new InMemoryLockManager();
        service = new CandleAggregationService(repository, activeCandleStore, lockManager);
    }
    
    @Test
    void testSingleEvent() {
        long timestamp = 1620000000L;
        BidAskEvent event = new BidAskEvent("BTC-USD", 50000.0, 50010.0, timestamp);
        
        service.processEvent(event);
        service.flushExpiredCandles(timestamp + 100);
        
        List<Candle> candles = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, timestamp, timestamp + 60);
        
        assertEquals(1, candles.size());
        Candle candle = candles.get(0);
        double expectedMid = (50000.0 + 50010.0) / 2.0;
        assertEquals(expectedMid, candle.getOpen(), 0.001);
        assertEquals(expectedMid, candle.getHigh(), 0.001);
        assertEquals(expectedMid, candle.getLow(), 0.001);
        assertEquals(expectedMid, candle.getClose(), 0.001);
        assertEquals(1, candle.getVolume());
    }
    
    @Test
    void testMultipleEvents() {
        long baseTimestamp = 1620000000L;
        BidAskEvent event1 = new BidAskEvent("BTC-USD", 50000.0, 50010.0, baseTimestamp);
        BidAskEvent event2 = new BidAskEvent("BTC-USD", 50020.0, 50030.0, baseTimestamp + 10);
        BidAskEvent event3 = new BidAskEvent("BTC-USD", 50005.0, 50015.0, baseTimestamp + 20);
        
        service.processEvent(event1);
        service.processEvent(event2);
        service.processEvent(event3);
        service.flushExpiredCandles(baseTimestamp + 100);
        
        List<Candle> candles = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, baseTimestamp, baseTimestamp + 60);
        
        assertEquals(1, candles.size());
        Candle candle = candles.get(0);
        
        double mid1 = (50000.0 + 50010.0) / 2.0;
        double mid2 = (50020.0 + 50030.0) / 2.0;
        double mid3 = (50005.0 + 50015.0) / 2.0;
        
        assertEquals(mid1, candle.getOpen(), 0.001);
        assertEquals(mid2, candle.getHigh(), 0.001);
        assertEquals(mid1, candle.getLow(), 0.001);
        assertEquals(mid3, candle.getClose(), 0.001);
        assertEquals(3, candle.getVolume());
    }
    
    @Test
    void testTimeframeBoundary() {
        long minute1 = 1620000000L;
        long minute2 = 1620000060L;
        
        BidAskEvent event1 = new BidAskEvent("BTC-USD", 50000.0, 50010.0, minute1);
        BidAskEvent event2 = new BidAskEvent("BTC-USD", 50020.0, 50030.0, minute2);
        
        service.processEvent(event1);
        service.processEvent(event2);
        service.flushExpiredCandles(minute2 + 100);
        
        List<Candle> candles = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, minute1, minute2 + 60);
        
        assertEquals(2, candles.size());
        assertEquals(minute1, candles.get(0).getTime());
        assertEquals(minute2, candles.get(1).getTime());
    }
    
    @Test
    void testInvalidEvent() {
        BidAskEvent invalidEvent = new BidAskEvent("BTC-USD", -100.0, 50010.0, 1620000000L);
        
        assertThrows(InvalidEventException.class, () -> {
            service.processEvent(invalidEvent);
        });
    }
}

