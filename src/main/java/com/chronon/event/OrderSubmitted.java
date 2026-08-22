package com.chronon.event;

import com.chronon.order.OrderType;
import com.chronon.order.Side;

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