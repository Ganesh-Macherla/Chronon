package com.chronon;

import com.chronon.bus.EventBus;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
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

    @Test
    void largeMarketOrderIsPartiallyFilled() {

        InMemoryEventStore store = new InMemoryEventStore();
        EventBus bus = new EventBus();

        LiveEventPipeline pipeline =
                new LiveEventPipeline(store, bus);

        MatchingEngine matchingEngine =
                new MatchingEngine(pipeline);

        bus.subscribe(matchingEngine);

        PriceUpdate firstPrice =
                new PriceUpdate(
                        pipeline.nextSequence(),
                        Instant.parse("2026-08-13T09:30:00Z"),
                        "AAPL",
                        new BigDecimal("104.00"),
                        100
                );

        pipeline.publish(firstPrice);

        OrderSubmitted order =
                new OrderSubmitted(
                        pipeline.nextSequence(),
                        firstPrice.timestamp(),
                        "ORD-002",
                        "AAPL",
                        Side.BUY,
                        250,
                        OrderType.MARKET,
                        null
                );

        pipeline.publish(order);

        List<Event> firstEvents = store.getAll();

        assertEquals(4, firstEvents.size());

        OrderPartiallyFilled partial =
                (OrderPartiallyFilled) firstEvents.get(3);

        assertEquals(
                "ORD-002",
                partial.orderId()
        );

        assertEquals(
                100,
                partial.filledQuantity()
        );

        assertEquals(
                new BigDecimal("104.00"),
                partial.fillPrice()
        );

        PriceUpdate secondPrice =
                new PriceUpdate(
                        pipeline.nextSequence(),
                        Instant.parse("2026-08-13T09:30:01Z"),
                        "AAPL",
                        new BigDecimal("105.00"),
                        150
                );

        pipeline.publish(secondPrice);

        List<Event> events = store.getAll();

        assertEquals(6, events.size());

        OrderFilled filled =
                (OrderFilled) events.get(5);

        assertEquals(
                "ORD-002",
                filled.orderId()
        );

        assertEquals(
                150,
                filled.filledQuantity()
        );

        assertEquals(
                new BigDecimal("105.00"),
                filled.fillPrice()
        );
    }
    @Test
void multipleOrdersAreMatchedInFifoOrder() {

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

    OrderSubmitted firstOrder =
            new OrderSubmitted(
                    pipeline.nextSequence(),
                    price.timestamp(),
                    "ORD-001",
                    "AAPL",
                    Side.BUY,
                    60,
                    OrderType.MARKET,
                    null
            );

    pipeline.publish(firstOrder);

    OrderSubmitted secondOrder =
            new OrderSubmitted(
                    pipeline.nextSequence(),
                    price.timestamp(),
                    "ORD-002",
                    "AAPL",
                    Side.BUY,
                    80,
                    OrderType.MARKET,
                    null
            );

    pipeline.publish(secondOrder);

    List<Event> events = store.getAll();

    OrderFilled firstFilled = null;
    OrderPartiallyFilled secondPartial = null;

    for (Event event : events) {

        if (event instanceof OrderFilled filled
                && filled.orderId().equals("ORD-001")) {
            firstFilled = filled;
        }

        if (event instanceof OrderPartiallyFilled partial
                && partial.orderId().equals("ORD-002")) {
            secondPartial = partial;
        }
    }

    assertNotNull(firstFilled);
    assertNotNull(secondPartial);

    assertEquals(
            60,
            firstFilled.filledQuantity()
    );

    assertEquals(
            40,
            secondPartial.filledQuantity()
    );

    assertEquals(
            new BigDecimal("104.00"),
            firstFilled.fillPrice()
    );

    assertEquals(
            new BigDecimal("104.00"),
            secondPartial.fillPrice()
    );
}
}