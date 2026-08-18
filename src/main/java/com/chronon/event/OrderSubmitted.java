package com.chronon.event;

import com.chronon.order.Order;

import java.time.Instant;

public record OrderSubmitted(
        long sequence,
        Instant timestamp,
        Order order
) implements Event {

    @Override
    public EventType type() {
        return EventType.ORDER_SUBMITTED;
    }
}