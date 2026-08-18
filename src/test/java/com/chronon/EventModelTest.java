package com.chronon;

import com.chronon.event.EventType;
import com.chronon.event.PriceUpdate;
import com.chronon.event.StrategyDecision;
import com.chronon.strategy.Action;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventModelTest {

    @Test
    void priceUpdateShouldBeAnEvent() {
        PriceUpdate event = new PriceUpdate(
                1,
                Instant.parse("2026-08-10T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        assertEquals(1, event.sequence());
        assertEquals("AAPL", event.symbol());
        assertEquals(new BigDecimal("210.40"), event.price());
        assertEquals(EventType.PRICE_UPDATE, event.type());
    }


    @Test
void strategyDecisionShouldStoreExplanation() {

    StrategyDecision event = new StrategyDecision(
            10,
            Instant.parse("2026-08-10T09:35:00Z"),
            "moving-average",
            Action.BUY,
            Map.of(
                    "shortMA", 210.42,
                    "longMA", 210.18
            ),
            "Short moving average is above long moving average.",
            0.82
    );

    assertEquals(10, event.sequence());
    assertEquals(EventType.STRATEGY_DECISION, event.type());
    assertEquals(Action.BUY, event.action());

    assertEquals(
            "Short moving average is above long moving average.",
            event.reasoning()
    );

    assertEquals(0.82, event.confidence());
}

}