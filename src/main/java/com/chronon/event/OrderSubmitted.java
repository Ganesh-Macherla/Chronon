package com.chronon.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSubmitted(
        long sequence,
        Instant timestamp,
        String orderId,
        String symbol,
        Side side,
        int quantity,
        OrderType orderType,
        BigDecimal limitPrice
) implements Event {

    @Override
    public EventType type() {
        return EventType.ORDER_SUBMITTED;
    }
}