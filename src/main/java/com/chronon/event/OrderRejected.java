package com.chronon.event;

import java.time.Instant;

public record OrderRejected(
        long sequence,
        Instant timestamp,
        String orderId,
        String reason
) implements Event {

    @Override
    public EventType type() {
        return EventType.ORDER_REJECTED;
    }
}