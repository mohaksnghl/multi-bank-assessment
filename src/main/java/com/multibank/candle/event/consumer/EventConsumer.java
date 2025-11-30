package com.multibank.candle.event.consumer;

import com.multibank.candle.model.BidAskEvent;

public interface EventConsumer {
    void consume(BidAskEvent event);
    
    // This method is needed for @EventListener to work with JDK proxies
    void handleBidAskEvent(BidAskEvent event);
}

