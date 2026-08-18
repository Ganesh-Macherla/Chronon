package com.chronon.event;

import com.chronon.strategy.Action;

import java.time.Instant;
import java.util.Map;

public record StrategyDecision(
        long sequence,
        Instant timestamp,
        String strategyId,
        Action action,
        Map<String, Object> signals,
        String reasoning,
        double confidence
) implements Event {

    @Override
    public EventType type() {
        return EventType.STRATEGY_DECISION;
    }
}