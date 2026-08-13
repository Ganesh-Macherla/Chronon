package com.chronon.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderFilled(
        long sequence,
        Instant timestamp,
        String orderId,
        int filledQuantity,
        BigDecimal fillPrice
) implements Event {

    @Override
    public EventType type() {
        return EventType.ORDER_FILLED;
    }
}