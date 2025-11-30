package com.multibank.candle.event.publisher;

import com.multibank.candle.model.BidAskEvent;
import lombok.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "event.publisher.type", havingValue = "spring", matchIfMissing = true)
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(@NonNull BidAskEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}

