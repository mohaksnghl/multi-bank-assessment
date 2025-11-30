package com.multibank.candle.controller;

import com.multibank.candle.model.Candle;
import com.multibank.candle.model.Timeframe;
import com.multibank.candle.repository.CandleRepository;
import com.multibank.candle.repository.InMemoryCandleRepository;
import com.multibank.candle.service.mapper.HistoryResponseMapper;
import com.multibank.candle.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HistoryControllerTest {
    
    private CandleRepository repository;
    private HistoryService historyService;
    private HistoryResponseMapper responseMapper;
    private HistoryController controller;
    
    @BeforeEach
    void setUp() {
        repository = new InMemoryCandleRepository();
        historyService = new HistoryService(repository);
        responseMapper = new HistoryResponseMapper();
        controller = new HistoryController(historyService, responseMapper);
        
        long baseTime = 1620000000L;
        for (int i = 0; i < 5; i++) {
            Candle candle = new Candle(
                baseTime + (i * 60),
                29500.5 + i,
                29510.0 + i,
                29490.0 + i,
                29505.0 + i,
                10 + i
            );
            repository.save("BTC-USD", Timeframe.ONE_MINUTE, candle);
        }
    }
    
    @Test
    void testValidRequest() {
        ResponseEntity<Map<String, Object>> response = controller.getHistory(
            "BTC-USD", "1m", 1620000000L, 1620000300L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals("ok", body.get("s"));
        
        @SuppressWarnings("unchecked")
        List<Long> times = (List<Long>) body.get("t");
        assertEquals(5, times.size());
    }
    
    @Test
    void testInvalidInterval() {
        ResponseEntity<Map<String, Object>> response = controller.getHistory(
            "BTC-USD", "invalid", 1620000000L, 1620000060L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody().get("s"));
    }
    
    @Test
    void testInvalidTimeRange() {
        ResponseEntity<Map<String, Object>> response = controller.getHistory(
            "BTC-USD", "1m", 1620000060L, 1620000000L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("error", response.getBody().get("s"));
    }
    
    @Test
    void testNoDataFound() {
        ResponseEntity<Map<String, Object>> response = controller.getHistory(
            "ETH-USD", "1m", 1620000000L, 1620000060L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals("ok", body.get("s"));
        
        @SuppressWarnings("unchecked")
        List<Long> times = (List<Long>) body.get("t");
        assertEquals(0, times.size());
    }
}

