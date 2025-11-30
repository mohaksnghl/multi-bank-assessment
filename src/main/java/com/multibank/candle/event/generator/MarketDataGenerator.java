package com.multibank.candle.event.generator;

import com.multibank.candle.config.ApplicationConfig;
import com.multibank.candle.event.publisher.EventPublisher;
import com.multibank.candle.model.BidAskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "market.generator.enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(MarketDataGenerator.class);
    
    private final ApplicationConfig config;
    private final EventPublisher eventPublisher;
    
    private final java.util.concurrent.ConcurrentHashMap<String, Double> currentPrices = 
        new java.util.concurrent.ConcurrentHashMap<>();
    
    private static final double SPREAD_PERCENTAGE = 0.001;
    
    public MarketDataGenerator(ApplicationConfig config, EventPublisher eventPublisher) {
        this.config = config;
        this.eventPublisher = eventPublisher;
        
        List<String> symbols = config.getSymbolList();
        for (String symbol : symbols) {
            double basePrice = config.getBasePrice(symbol);
            currentPrices.put(symbol, basePrice);
        }
        
        logger.info("MarketDataGenerator initialized with {} symbols: {}", 
            symbols.size(), symbols);
    }
    
    @Scheduled(fixedRate = 1000)
    public void generateEvents() {
        if (!config.isEnabled()) return;

        List<String> symbols = config.getSymbolList();
        int eventsPerSymbol = config.getRatePerSecond();

        long currentTimestamp = System.currentTimeMillis() / 1000;
        int totalEvents = 0;

        logger.info("Generating events batch: timestamp={}, symbols={}, eventsPerSymbol={}", 
            currentTimestamp, symbols.size(), eventsPerSymbol);

        for (String symbol : symbols) {
            for (int i = 0; i < eventsPerSymbol; i++) {
                BidAskEvent event = generateEvent(symbol, currentTimestamp);
                eventPublisher.publish(event);
                totalEvents++;
                logger.debug("Generated and published event: symbol={}, bid={}, ask={}, timestamp={}", 
                    event.getSymbol(), event.getBid(), event.getAsk(), event.getTimestamp());
            }
        }
        
        logger.info("Generated {} events for {} symbols ({} events/symbol) at timestamp {}", 
            totalEvents, symbols.size(), eventsPerSymbol, currentTimestamp);
    }
    
    private BidAskEvent generateEvent(String symbol, long timestamp) {
        double currentPrice = currentPrices.getOrDefault(symbol, config.getBasePrice(symbol));

        // Simulate price change
        double changePercent = ThreadLocalRandom.current().nextGaussian() * 0.005;
        double newPrice = currentPrice * (1.0 + changePercent);

        currentPrices.put(symbol, newPrice);
        
        double spread = newPrice * SPREAD_PERCENTAGE;
        double bid = newPrice - spread / 2.0;
        double ask = newPrice + spread / 2.0;

        // Simulate spread distribution
        double spreadVariation = ThreadLocalRandom.current().nextGaussian() * (spread * 0.1);
        bid += spreadVariation;
        ask += spreadVariation;

        double[] bidAsk = ensureBidAskOrder(bid, ask);
        return new BidAskEvent(symbol, bidAsk[0], bidAsk[1], timestamp);
    }

    private double[] ensureBidAskOrder(double bid, double ask) {
        if (ask < bid) {
            return new double[]{ask, bid};  // Swap them
        }
        return new double[]{bid, ask};
    }
}

