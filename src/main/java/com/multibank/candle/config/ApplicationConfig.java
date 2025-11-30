package com.multibank.candle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix = "market.generator")
public class ApplicationConfig {

    @Setter
    private Boolean enabled;
    @Setter
    private Integer ratePerSecond;
    @Getter
    @Setter
    private String symbols;
    @Getter
    private Map<String, Double> basePrices = new HashMap<>();

    public boolean isEnabled() {
        return enabled != null ? enabled : false;
    }

    public int getRatePerSecond() {
        return ratePerSecond != null ? ratePerSecond : 0;
    }
    
    public List<String> getSymbolList() {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(symbols.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
    
    public void setBasePrices(Map<String, Double> basePrices) {
        this.basePrices = basePrices != null ? basePrices : new HashMap<>();
    }
    
    public double getBasePrice(String symbol) {
        if (!basePrices.containsKey(symbol)) {
            throw new IllegalArgumentException("Base price not configured for symbol: " + symbol);
        }
        return basePrices.get(symbol);
    }
}

