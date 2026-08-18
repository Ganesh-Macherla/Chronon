package com.chronon;

import com.chronon.event.PriceUpdate;
import com.chronon.strategy.Action;
import com.chronon.strategy.Decision;
import com.chronon.strategy.MovingAverageStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MovingAverageStrategyTest {

    @Test
    void holdsUntilEnoughPricesAreAvailable() {

        MovingAverageStrategy strategy =
                new MovingAverageStrategy(2, 4);

        Decision decision = strategy.onPriceUpdate(
                price(1, "100")
        );

        assertEquals(Action.HOLD, decision.action());
        assertEquals(1, decision.signals().get("pricesCollected"));
    }

    @Test
    void buysWhenShortAverageIsAboveLongAverage() {

        MovingAverageStrategy strategy =
                new MovingAverageStrategy(2, 4);

        strategy.onPriceUpdate(price(1, "100"));
        strategy.onPriceUpdate(price(2, "101"));
        strategy.onPriceUpdate(price(3, "102"));

        Decision decision =
                strategy.onPriceUpdate(price(4, "104"));

        assertEquals(Action.BUY, decision.action());

        assertTrue(
                (double) decision.signals().get("shortMA")
                        >
                (double) decision.signals().get("longMA")
        );
    }

    @Test
    void sellsWhenShortAverageIsBelowLongAverage() {

        MovingAverageStrategy strategy =
                new MovingAverageStrategy(2, 4);

        strategy.onPriceUpdate(price(1, "104"));
        strategy.onPriceUpdate(price(2, "103"));
        strategy.onPriceUpdate(price(3, "102"));

        Decision decision =
                strategy.onPriceUpdate(price(4, "100"));

        assertEquals(Action.SELL, decision.action());

        assertTrue(
                (double) decision.signals().get("shortMA")
                        <
                (double) decision.signals().get("longMA")
        );
    }

    private PriceUpdate price(long sequence, String price) {

        return new PriceUpdate(
                sequence,
                Instant.parse("2026-08-13T09:30:00Z")
                        .plusSeconds(sequence),
                "AAPL",
                new BigDecimal(price),
                100
        );
    }
}