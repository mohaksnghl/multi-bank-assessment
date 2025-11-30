package com.multibank.candle.service;

import com.multibank.candle.exception.InvalidRequestException;
import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import com.multibank.candle.repository.CandleRepository;
import com.multibank.candle.util.TimeframeUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {
    
    private final CandleRepository candleRepository;
    
    public HistoryService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }
    
    public List<Candle> getHistory(String symbol, String interval, long from, long to) {
        validateRequest(symbol, interval, from, to);
        
        Timeframe timeframe;
        try {
            timeframe = TimeframeUtil.parseInterval(interval);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
        
        String normalizedSymbol = symbol.trim();
        
        return candleRepository.findBySymbolAndInterval(normalizedSymbol, timeframe, from, to);
    }
    
    private void validateRequest(String symbol, String interval, long from, long to) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidRequestException("Invalid symbol parameter");
        }
        
        if (from >= to) {
            throw new InvalidRequestException("Parameter 'from' must be less than 'to'");
        }
        
        if (interval == null || interval.trim().isEmpty()) {
            throw new InvalidRequestException("Invalid interval parameter");
        }
    }
}

