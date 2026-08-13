package com.chronon.event;

import java.time.Instant;

public sealed interface Event
        permits PriceUpdate,
                OrderSubmitted,
                OrderAccepted,
                OrderPartiallyFilled,
                OrderFilled,
                OrderRejected,
                StrategyDecision {

    long sequence();

    Instant timestamp();

    EventType type();
}