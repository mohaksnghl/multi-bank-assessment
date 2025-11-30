package com.multibank.candle.controller;

import com.multibank.candle.exception.InvalidRequestException;
import com.multibank.candle.model.Candle;
import com.multibank.candle.service.mapper.HistoryResponseMapper;
import com.multibank.candle.service.HistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryController.class);
    
    private final HistoryService historyService;
    private final HistoryResponseMapper responseMapper;
    
    public HistoryController(HistoryService historyService, HistoryResponseMapper responseMapper) {
        this.historyService = historyService;
        this.responseMapper = responseMapper;
    }
    
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @RequestParam String symbol,
            @RequestParam String interval,
            @RequestParam long from,
            @RequestParam long to) {
        
        try {
            List<Candle> candles = historyService.getHistory(symbol, interval, from, to);
            Map<String, Object> response = responseMapper.toResponse(candles);
            return ResponseEntity.ok(response);
            
        } catch (InvalidRequestException e) {
            logger.warn("History request error: {}", e.getMessage());
            Map<String, Object> errorResponse = responseMapper.toErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

