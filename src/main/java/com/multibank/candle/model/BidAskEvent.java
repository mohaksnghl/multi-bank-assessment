package com.multibank.candle.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class BidAskEvent {
    
    private final String symbol;
    private final double bid;
    private final double ask;
    private final long timestamp;

    public double midPrice() {
        return (bid + ask) / 2.0;
    }
    
    public boolean isValid() {
        return bid > 0 && ask > 0 && ask >= bid && timestamp > 0;
    }
}
