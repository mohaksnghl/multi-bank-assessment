package com.multibank.candle.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Candle {
    
    private final long time;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;

    @Getter
    public static class Builder {
        private final long timestamp;
        private double open;
        private double high = Double.NEGATIVE_INFINITY;
        private double low = Double.POSITIVE_INFINITY;
        private double close;
        private long volume;
        private boolean hasData = false;
        
        public Builder(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public void addPrice(double price) {
            if (!hasData) {
                open = high = low = close = price;
                hasData = true;
            } else {
                high = Math.max(high, price);
                low = Math.min(low, price);
                close = price;
            }
            volume++;
        }
        
        public Candle build() {
            if (!hasData) {
                throw new IllegalStateException("Cannot build candle with no prices");
            }
            return new Candle(timestamp, open, high, low, close, volume);
        }
    }
}
