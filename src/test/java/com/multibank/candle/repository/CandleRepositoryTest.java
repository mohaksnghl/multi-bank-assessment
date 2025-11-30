package com.multibank.candle.repository;

import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandleRepositoryTest {
    
    private InMemoryCandleRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryCandleRepository();
    }
    
    @Test
    void testSaveAndRetrieve() {
        Candle candle = new Candle(1620000000L, 100.0, 110.0, 95.0, 105.0, 100);
        
        repository.save("BTC-USD", Timeframe.ONE_MINUTE, candle);
        List<Candle> retrieved = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, 1620000000L, 1620000060L);
        
        assertEquals(1, retrieved.size());
        assertEquals(candle, retrieved.get(0));
    }
    
    @Test
    void testRangeQuery() {
        for (int i = 0; i < 10; i++) {
            Candle candle = new Candle(
                1620000000L + (i * 60),
                100.0 + i,
                110.0 + i,
                95.0 + i,
                105.0 + i,
                10
            );
            repository.save("BTC-USD", Timeframe.ONE_MINUTE, candle);
        }
        
        List<Candle> candles = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, 1620000000L, 1620000000L + (5 * 60));
        
        assertEquals(5, candles.size());
    }
    
    @Test
    void testEmptyResult() {
        List<Candle> candles = repository.findBySymbolAndInterval(
            "ETH-USD", Timeframe.ONE_MINUTE, 1620000000L, 1620000060L);
        
        assertTrue(candles.isEmpty());
    }
    
    @Test
    void testMultipleSymbols() {
        Candle btcCandle = new Candle(1620000000L, 50000.0, 51000.0, 49000.0, 50500.0, 100);
        Candle ethCandle = new Candle(1620000000L, 3000.0, 3100.0, 2900.0, 3050.0, 50);
        
        repository.save("BTC-USD", Timeframe.ONE_MINUTE, btcCandle);
        repository.save("ETH-USD", Timeframe.ONE_MINUTE, ethCandle);
        
        List<Candle> btcCandles = repository.findBySymbolAndInterval(
            "BTC-USD", Timeframe.ONE_MINUTE, 1620000000L, 1620000060L);
        List<Candle> ethCandles = repository.findBySymbolAndInterval(
            "ETH-USD", Timeframe.ONE_MINUTE, 1620000000L, 1620000060L);
        
        assertEquals(1, btcCandles.size());
        assertEquals(1, ethCandles.size());
    }
}

