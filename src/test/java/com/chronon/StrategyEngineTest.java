package com.chronon;

import com.chronon.bus.EventBus;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.event.StrategyDecision;
import com.chronon.order.OrderType;
import com.chronon.order.Side;
import com.chronon.store.InMemoryEventStore;
import com.chronon.strategy.MovingAverageStrategy;
import com.chronon.strategy.StrategyEngine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StrategyEngineTest {

    @Test
    void priceUpdatesProduceStrategyDecisionEvents() {

        InMemoryEventStore store = new InMemoryEventStore();
        EventBus bus = new EventBus();

        LiveEventPipeline pipeline =
                new LiveEventPipeline(store, bus);

        MovingAverageStrategy strategy =
                new MovingAverageStrategy(2, 4);

        StrategyEngine engine =
                new StrategyEngine(
                        "moving-average",
                        strategy,
                        pipeline
                );

        bus.subscribe(engine);

        pipeline.publish(price(pipeline.nextSequence(), "100"));
        pipeline.publish(price(pipeline.nextSequence(), "101"));
        pipeline.publish(price(pipeline.nextSequence(), "102"));
        pipeline.publish(price(pipeline.nextSequence(), "104"));

        List<Event> events = store.getAll();

        // 4 PriceUpdates + 4 StrategyDecisions + 1 OrderSubmitted
        assertEquals(9, events.size());

        // Every event must have a unique global sequence.
        for (int i = 0; i < events.size(); i++) {
            assertEquals(i + 1, events.get(i).sequence());
        }

        StrategyDecision decision =
                (StrategyDecision) events.get(7);

        assertEquals(
                "moving-average",
                decision.strategyId()
        );

        assertEquals(
                "BUY",
                decision.action().name()
        );

        assertEquals(
                0.5,
                decision.confidence()
        );

        assertFalse(
                decision.reasoning().isBlank()
        );
    }

    @Test
    void buyDecisionProducesMarketOrder() {

        InMemoryEventStore store = new InMemoryEventStore();
        EventBus bus = new EventBus();

        LiveEventPipeline pipeline =
                new LiveEventPipeline(store, bus);

        MovingAverageStrategy strategy =
                new MovingAverageStrategy(2, 4);

        StrategyEngine engine =
                new StrategyEngine(
                        "moving-average",
                        strategy,
                        pipeline
                );

        bus.subscribe(engine);

        pipeline.publish(price(pipeline.nextSequence(), "100"));
        pipeline.publish(price(pipeline.nextSequence(), "101"));
        pipeline.publish(price(pipeline.nextSequence(), "102"));
        pipeline.publish(price(pipeline.nextSequence(), "104"));

        List<Event> events = store.getAll();

        OrderSubmitted order =
                (OrderSubmitted) events.get(8);

        assertEquals(
                "AAPL",
                order.symbol()
        );

        assertEquals(
                Side.BUY,
                order.side()
        );

        assertEquals(
                100,
                order.quantity()
        );

        assertEquals(
                OrderType.MARKET,
                order.orderType()
        );

        assertNull(order.limitPrice());
    }

    private PriceUpdate price(
            long sequence,
            String price
    ) {

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