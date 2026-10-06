package com.chronon.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdate(
        long sequence,
        Instant timestamp,
        String symbol,
        BigDecimal price,
        int volume) implements Event {

    public PriceUpdate {
        if (volume < 0) {
            throw new IllegalArgumentException(
                    "Price update volume cannot be negative"
            );
        }
    }

    @Override
    public EventType type() {
        return EventType.PRICE_UPDATE;
    }
}