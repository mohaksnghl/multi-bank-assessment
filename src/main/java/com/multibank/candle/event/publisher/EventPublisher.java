package com.multibank.candle.event.publisher;

import com.multibank.candle.model.BidAskEvent;

public interface EventPublisher {
    void publish(BidAskEvent event);
}

