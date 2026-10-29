package com.chronon;

import com.chronon.replay.EventExplainer;
import com.chronon.event.PriceUpdate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EventExplainerTest {

    @Test
    void explainsPriceUpdate() {
        PriceUpdate event = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:01Z"),
                "AAPL",
                new BigDecimal("105.00"),
                100
        );

        EventExplainer explainer = new EventExplainer();

        String explanation = explainer.explain(event);

        assertTrue(explanation.contains("AAPL"));
        assertTrue(explanation.contains("105.00"));
        assertTrue(explanation.contains("100"));
        assertTrue(explanation.contains("Sequence: 1"));
        assertTrue(explanation.contains("2026-08-13T09:30:01Z"));
    }

    @Test
    void rejectsNullEvent() {
        EventExplainer explainer = new EventExplainer();

        assertThrows(
                IllegalArgumentException.class,
                () -> explainer.explain(null)
        );
    }
}
