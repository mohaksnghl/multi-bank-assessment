package com.multibank.candle.exception;

import com.multibank.candle.model.BidAskEvent;
import lombok.Getter;

@Getter
public class InvalidEventException extends RuntimeException {
    
    private final BidAskEvent event;

    public InvalidEventException(BidAskEvent event) {
        super("Invalid bid/ask event: " + event);
        this.event = event;
    }

}

