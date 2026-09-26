package com.chronon;

import com.chronon.event.OrderFilled;
import com.chronon.event.PriceUpdate;
import com.chronon.replay.EventExplainer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventExplainerTest {

    @Test
    void explainsPriceUpdate() {

        EventExplainer explainer =
                new EventExplainer();

        PriceUpdate event =
                new PriceUpdate(
                        1,
                        Instant.parse("2026-08-13T09:30:00Z"),
                        "AAPL",
                        new BigDecimal("104.00"),
                        100
                );

        String explanation =
                explainer.explain(event);

        assertEquals(
                "Price update: AAPL traded at 104.00 with volume 100.",
                explanation
        );
    }

    @Test
    void explainsOrderFilled() {

        EventExplainer explainer =
                new EventExplainer();

        OrderFilled event =
                new OrderFilled(
                        5,
                        Instant.parse("2026-08-13T09:30:05Z"),
                        "ORD-001",
                        100,
                        new BigDecimal("104.00")
                );

        String explanation =
                explainer.explain(event);

        assertEquals(
                "Order ORD-001 was completely filled: 100 units at 104.00.",
                explanation
        );
    }

    @Test
    void rejectsNullEvent() {

        EventExplainer explainer =
                new EventExplainer();

        assertThrows(
                IllegalArgumentException.class,
                () -> explainer.explain(null)
        );
    }
}