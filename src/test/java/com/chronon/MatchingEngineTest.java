package com.chronon;

import com.chronon.bus.EventBus;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.matching.MatchingEngine;
import com.chronon.order.OrderType;
import com.chronon.order.Side;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    @Test
    void marketOrderIsAcceptedAndFilled() {

        InMemoryEventStore store = new InMemoryEventStore();
        EventBus bus = new EventBus();

        LiveEventPipeline pipeline =
                new LiveEventPipeline(store, bus);

        MatchingEngine matchingEngine =
                new MatchingEngine(pipeline);

        bus.subscribe(matchingEngine);

        PriceUpdate price =
                new PriceUpdate(
                        pipeline.nextSequence(),
                        Instant.parse("2026-08-13T09:30:00Z"),
                        "AAPL",
                        new BigDecimal("104.00"),
                        100
                );

        pipeline.publish(price);

        OrderSubmitted order =
                new OrderSubmitted(
                        pipeline.nextSequence(),
                        price.timestamp(),
                        "ORD-001",
                        "AAPL",
                        Side.BUY,
                        100,
                        OrderType.MARKET,
                        null
                );

        pipeline.publish(order);

        List<Event> events = store.getAll();

        assertEquals(4, events.size());

        OrderAccepted accepted =
                (OrderAccepted) events.get(2);

        assertEquals(
                "ORD-001",
                accepted.orderId()
        );

        OrderFilled filled =
                (OrderFilled) events.get(3);

        assertEquals(
                "ORD-001",
                filled.orderId()
        );

        assertEquals(
                100,
                filled.filledQuantity()
        );

        assertEquals(
                new BigDecimal("104.00"),
                filled.fillPrice()
        );
    }
}