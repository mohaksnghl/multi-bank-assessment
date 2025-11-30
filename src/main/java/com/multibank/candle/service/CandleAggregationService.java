package com.multibank.candle.service;

import com.multibank.candle.exception.InvalidEventException;
import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import com.multibank.candle.repository.CandleRepository;
import com.multibank.candle.service.storage.ActiveCandleStore;
import com.multibank.candle.service.storage.LockManager;
import com.multibank.candle.util.CandleKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
public class CandleAggregationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CandleAggregationService.class);
    
    private final CandleRepository candleRepository;
    private final ActiveCandleStore activeCandleStore;
    private final LockManager lockManager;
    
    public CandleAggregationService(
            CandleRepository candleRepository,
            ActiveCandleStore activeCandleStore,
            LockManager lockManager) {
        this.candleRepository = candleRepository;
        this.activeCandleStore = activeCandleStore;
        this.lockManager = lockManager;
    }
    
    public void processEvent(BidAskEvent event) {
        if (!event.isValid()) {
            logger.warn("Invalid event received: {}", event);
            throw new InvalidEventException(event);
        }
        
        double midPrice = event.midPrice();
        
        for (Timeframe timeframe : Timeframe.values()) {
            processEventForTimeframe(event, midPrice, timeframe);
        }
    }
    
    private void processEventForTimeframe(BidAskEvent event, double midPrice, Timeframe timeframe) {
        long timeWindowStart = timeframe.getWindowStartTimestamp(event.getTimestamp());
        String key = CandleKeyUtil.buildKey(event.getSymbol(), timeframe, timeWindowStart);
        
        // Check for and flush any old candles for this symbol+timeframe with different time windows
        flushOldCandlesForTimeframe(event.getSymbol(), timeframe, timeWindowStart);
        
        Lock lock = lockManager.getLock(key);
        lock.lock();
        try {
            // Get or create builder for current time window
            Candle.Builder candleBuilder = activeCandleStore.computeIfAbsent(
                key, 
                k -> {
                    logger.info("Creating new candle: symbol={}, timeframe={}, timeWindow={}, eventTimestamp={}", 
                        event.getSymbol(), timeframe.getCode(), timeWindowStart, event.getTimestamp());
                    return new Candle.Builder(timeWindowStart);
                }
            );
            
            candleBuilder.addPrice(midPrice);
            
            logger.debug("Updated candle: symbol={}, timeframe={}, timeWindow={}, volume={}", 
                event.getSymbol(), timeframe.getCode(), timeWindowStart, candleBuilder.build().getVolume());
            
        } finally {
            lock.unlock();
        }
    }
    
    private void flushOldCandlesForTimeframe(String symbol, Timeframe timeframe, long currentTimeWindow) {
        activeCandleStore.removeIf(entry -> {
            String key = entry.getKey();
            Candle.Builder builder = entry.getValue();
            
            CandleKeyUtil.KeyInfo keyInfo = CandleKeyUtil.parseKey(key);
            if (keyInfo == null) {
                return false;
            }
            
            // Check if this is the same symbol and timeframe but different time window
            if (keyInfo.symbol().equals(symbol) && 
                keyInfo.timeframe() == timeframe && 
                builder.getTimestamp() != currentTimeWindow) {
                
                logger.info("Flushing old candle: symbol={}, timeframe={}, oldWindow={}, newWindow={}", 
                    symbol, timeframe.getCode(), builder.getTimestamp(), currentTimeWindow);
                flushCandle(key, symbol, timeframe, builder);
                return true;
            }
            
            return false;
        });
    }
    
    // Package-private method for testing only - forces flush of expired candles
    void flushExpiredCandles(long currentTimestamp) {
        int[] flushedCount = {0};
        activeCandleStore.removeIf(entry -> {
            String key = entry.getKey();
            Candle.Builder builder = entry.getValue();
            
            CandleKeyUtil.KeyInfo keyInfo = CandleKeyUtil.parseKey(key);
            if (keyInfo == null) {
                logger.warn("Unable to parse candle key: {}", key);
                return false;
            }
            
            long candleEndTime = builder.getTimestamp() + keyInfo.timeframe().getSeconds();
            if (currentTimestamp >= candleEndTime) {
                flushCandle(key, keyInfo.symbol(), keyInfo.timeframe(), builder);
                flushedCount[0]++;
                return true;
            }
            return false;
        });
        
        if (flushedCount[0] > 0) {
            logger.debug("Flushed {} expired candles", flushedCount[0]);
        }
    }
    
    private void flushCandle(String key, String symbol, Timeframe timeframe, Candle.Builder builder) {
        Candle candle = builder.build();
        candleRepository.save(symbol, timeframe, candle);
        activeCandleStore.remove(key);
        lockManager.removeLock(key);
        
        logger.info("Flushed candle: symbol={}, timeframe={}, time={}, volume={}, OHLC=[{},{},{},{}]", 
            symbol, timeframe.getCode(), candle.getTime(), candle.getVolume(),
            candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose());
    }
}
