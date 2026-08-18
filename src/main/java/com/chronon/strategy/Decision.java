package com.chronon.strategy;

import java.util.Map;

public record Decision(
        Action action,
        Map<String, Object> signals,
        String reasoning,
        double confidence
) {
}