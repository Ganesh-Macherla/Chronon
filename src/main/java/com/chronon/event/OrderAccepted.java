package com.chronon.event;

import java.time.Instant;

public record OrderAccepted(
        long sequence,
        Instant timestamp,
        String orderId
) implements Event {

    @Override
    public EventType type() {
        return EventType.ORDER_ACCEPTED;
    }
}