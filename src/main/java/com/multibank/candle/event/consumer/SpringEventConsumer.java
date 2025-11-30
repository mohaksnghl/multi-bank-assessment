package com.multibank.candle.event.consumer;

import com.multibank.candle.exception.InvalidEventException;
import com.multibank.candle.model.BidAskEvent;
import com.multibank.candle.service.CandleAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "event.consumer.type", havingValue = "spring", matchIfMissing = true)
public class SpringEventConsumer implements EventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringEventConsumer.class);
    
    private final CandleAggregationService aggregationService;
    
    public SpringEventConsumer(CandleAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }
    
    @Override
    @EventListener
    @Async
    public void handleBidAskEvent(BidAskEvent event) {
        consume(event);
    }
    
    @Override
    public void consume(BidAskEvent event) {
        try {
            aggregationService.processEvent(event);
            logger.debug("Successfully processed event: symbol={}, timestamp={}", 
                event.getSymbol(), event.getTimestamp());
        } catch (InvalidEventException e) {
            logger.warn("Invalid event rejected: {}", e.getMessage());
        }
    }
}

