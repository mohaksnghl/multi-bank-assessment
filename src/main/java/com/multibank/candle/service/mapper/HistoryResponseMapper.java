package com.multibank.candle.service.mapper;

import com.multibank.candle.model.Candle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HistoryResponseMapper {
    
    public Map<String, Object> toResponse(List<Candle> candles) {
        Map<String, Object> response = new HashMap<>();
        response.put("s", "ok");

        List<Long> times = new ArrayList<>();
        List<Double> opens = new ArrayList<>();
        List<Double> highs = new ArrayList<>();
        List<Double> lows = new ArrayList<>();
        List<Double> closes = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        for (Candle candle : candles) {
            times.add(candle.getTime());
            opens.add(candle.getOpen());
            highs.add(candle.getHigh());
            lows.add(candle.getLow());
            closes.add(candle.getClose());
            volumes.add(candle.getVolume());
        }

        response.put("t", times);
        response.put("o", opens);
        response.put("h", highs);
        response.put("l", lows);
        response.put("c", closes);
        response.put("v", volumes);
        
        return response;
    }
    
    public Map<String, Object> toErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("s", "error");
        response.put("errmsg", message);
        return response;
    }
}
