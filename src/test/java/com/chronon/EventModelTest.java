package com.chronon;

import com.chronon.event.EventType;
import com.chronon.event.PriceUpdate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

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
}