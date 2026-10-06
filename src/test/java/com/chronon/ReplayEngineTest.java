package com.chronon;

import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderSubmitted;
import com.chronon.order.OrderState;
import com.chronon.order.OrderStateProjector;
import com.chronon.order.OrderStatus;
import com.chronon.order.OrderType;
import com.chronon.order.Side;
import com.chronon.order.OrderStateReducer;
import com.chronon.bus.EventBus;
import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.PriceUpdate;
import com.chronon.replay.ReplayEngine;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReplayEngineTest {

    @Test
    void replayReadsEventsInSequenceOrder() {

        InMemoryEventStore store = new InMemoryEventStore();

        store.append(new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:01Z"),
                "AAPL",
                new BigDecimal("100.00"),
                100
        ));

        store.append(new PriceUpdate(
                2,
                Instant.parse("2026-08-13T09:30:02Z"),
                "AAPL",
                new BigDecimal("101.00"),
                100
        ));

        store.append(new PriceUpdate(
                3,
                Instant.parse("2026-08-13T09:30:03Z"),
                "AAPL",
                new BigDecimal("102.00"),
                100
        ));

        ReplayEngine replayEngine =
                new ReplayEngine(store);

        List<Long> sequences = new ArrayList<>();

        EventListener listener =
                event -> sequences.add(event.sequence());

        replayEngine.replay(listener);

        assertEquals(
                List.of(1L, 2L, 3L),
                sequences
        );

        assertEquals(3, store.size());
    }

    @Test
    void replayRejectsNullListener() {

        InMemoryEventStore store = new InMemoryEventStore();

        ReplayEngine replayEngine =
                new ReplayEngine(store);

        assertThrows(
                IllegalArgumentException.class,
                () -> replayEngine.replay(null)
        );
    }

    @Test
    void replayEngineRejectsNullEventStore() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplayEngine(null)
        );
    }

    @Test
    void replayDoesNotModifyEventStore() {

        InMemoryEventStore store = new InMemoryEventStore();

        store.append(new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:01Z"),
                "AAPL",
                new BigDecimal("100.00"),
                100
        ));

        ReplayEngine replayEngine =
                new ReplayEngine(store);

        List<Long> sequences = new ArrayList<>();

        replayEngine.replay(
                event -> sequences.add(event.sequence())
        );

        assertEquals(1, store.size());
        assertEquals(List.of(1L), sequences);
    }
    @Test
void replayReconstructsFinalOrderState() {

    InMemoryEventStore store = new InMemoryEventStore();

    Instant time =
            Instant.parse("2026-08-13T09:30:00Z");

    store.append(
            new OrderSubmitted(
                    1,
                    time,
                    "ORD-001",
                    "AAPL",
                    Side.BUY,
                    100,
                    OrderType.MARKET,
                    null
            )
    );

    store.append(
            new OrderAccepted(
                    2,
                    time,
                    "ORD-001"
            )
    );

    store.append(
            new OrderPartiallyFilled(
                    3,
                    time,
                    "ORD-001",
                    40,
                    new BigDecimal("100.00")
            )
    );

    store.append(
            new OrderFilled(
                    4,
                    time,
                    "ORD-001",
                    60,
                    new BigDecimal("110.00")
            )
    );

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    OrderStateProjector projector =
            new OrderStateProjector();

    replayEngine.replay(projector);

    OrderState state =
            projector.getState("ORD-001");

    assertNotNull(state);

    assertEquals(
            OrderStatus.FILLED,
            state.status()
    );

    assertEquals(
            100,
            state.quantity()
    );

    assertEquals(
            100,
            state.filledQuantity()
    );

    assertEquals(
            0,
            state.remainingQuantity()
    );

    assertEquals(
            new BigDecimal("106.00000000"),
            state.averageFillPrice()
    );
}

@Test
void replayRangeReplaysOnlyEventsInsideRange() {

    InMemoryEventStore store = new InMemoryEventStore();
    EventBus bus = new EventBus();

    LiveEventPipeline pipeline =
            new LiveEventPipeline(store, bus);

    List<Event> received = new ArrayList<>();

    pipeline.publish(
            new PriceUpdate(
                    pipeline.nextSequence(),
                    Instant.parse("2026-08-13T09:30:00Z"),
                    "AAPL",
                    new BigDecimal("104.00"),
                    100
            )
    );

    pipeline.publish(
            new PriceUpdate(
                    pipeline.nextSequence(),
                    Instant.parse("2026-08-13T09:31:00Z"),
                    "AAPL",
                    new BigDecimal("105.00"),
                    150
            )
    );

    pipeline.publish(
            new PriceUpdate(
                    pipeline.nextSequence(),
                    Instant.parse("2026-08-13T09:32:00Z"),
                    "AAPL",
                    new BigDecimal("106.00"),
                    200
            )
    );

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    replayEngine.replayRange(
            2,
            3,
            received::add
    );

    assertEquals(2, received.size());
    assertEquals(2, received.get(0).sequence());
    assertEquals(3, received.get(1).sequence());
}

@Test
void replayRangeRejectsInvalidRange() {

    InMemoryEventStore store =
            new InMemoryEventStore();

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    assertThrows(
            IllegalArgumentException.class,
            () -> replayEngine.replayRange(
                    5,
                    2,
                    event -> {}
            )
    );
}

@Test
void reconstructBuildsOrderStateWithoutLivePipeline() {

    InMemoryEventStore store =
            new InMemoryEventStore();

    Instant time =
            Instant.parse("2026-08-13T09:30:00Z");

    store.append(
            new OrderSubmitted(
                    1,
                    time,
                    "ORD-001",
                    "AAPL",
                    Side.BUY,
                    100,
                    OrderType.MARKET,
                    null
            )
    );

    store.append(
            new OrderAccepted(
                    2,
                    time,
                    "ORD-001"
            )
    );

    store.append(
            new OrderPartiallyFilled(
                    3,
                    time,
                    "ORD-001",
                    40,
                    new BigDecimal("100.00")
            )
    );

    store.append(
            new OrderFilled(
                    4,
                    time,
                    "ORD-001",
                    60,
                    new BigDecimal("110.00")
            )
    );

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    OrderStateReducer reducer =
            new OrderStateReducer();

    Map<String, OrderState> states =
            replayEngine.reconstruct(
                    Map.of(),
                    reducer
            );

    OrderState state =
            states.get("ORD-001");

    assertNotNull(state);

    assertEquals(
            OrderStatus.FILLED,
            state.status()
    );

    assertEquals(
            100,
            state.filledQuantity()
    );

    assertEquals(
            0,
            state.remainingQuantity()
    );

    assertEquals(
            new BigDecimal("106.00000000"),
            state.averageFillPrice()
    );
}

@Test
void reconstructRangeBuildsStateAtSpecificPointInHistory() {

    InMemoryEventStore store =
            new InMemoryEventStore();

    Instant time =
            Instant.parse("2026-08-13T09:30:00Z");

    store.append(
            new OrderSubmitted(
                    1,
                    time,
                    "ORD-001",
                    "AAPL",
                    Side.BUY,
                    100,
                    OrderType.MARKET,
                    null
            )
    );

    store.append(
            new OrderAccepted(
                    2,
                    time,
                    "ORD-001"
            )
    );

    store.append(
            new OrderPartiallyFilled(
                    3,
                    time,
                    "ORD-001",
                    40,
                    new BigDecimal("100.00")
            )
    );

    store.append(
            new OrderFilled(
                    4,
                    time,
                    "ORD-001",
                    60,
                    new BigDecimal("110.00")
            )
    );

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    OrderStateReducer reducer =
            new OrderStateReducer();

    Map<String, OrderState> states =
            replayEngine.reconstructRange(
                    1,
                    3,
                    Map.of(),
                    reducer
            );

    OrderState state =
            states.get("ORD-001");

    assertNotNull(state);

    assertEquals(
            OrderStatus.PARTIALLY_FILLED,
            state.status()
    );

    assertEquals(
            40,
            state.filledQuantity()
    );

    assertEquals(
            60,
            state.remainingQuantity()
    );

    assertEquals(
            new BigDecimal("100.00"),
            state.averageFillPrice()
    );
}

@Test
void replayProducesDeterministicResult() {

    InMemoryEventStore store =
            new InMemoryEventStore();

    Instant time =
            Instant.parse("2026-08-13T09:30:00Z");

    store.append(
            new OrderSubmitted(
                    1,
                    time,
                    "ORD-001",
                    "AAPL",
                    Side.BUY,
                    100,
                    OrderType.MARKET,
                    null
            )
    );

    store.append(
            new OrderAccepted(
                    2,
                    time,
                    "ORD-001"));

    store.append(new OrderPartiallyFilled(
                    3,
                    time,
                    "ORD-001",
                    40,
                    new BigDecimal("100.00")));

    store.append(
            new OrderFilled(
                    4,
                    time,
                    "ORD-001",
                    60,
                    new BigDecimal("110.00")
            )
    );

    ReplayEngine replayEngine =
            new ReplayEngine(store);

    OrderStateProjector firstProjector =
            new OrderStateProjector();

    OrderStateProjector secondProjector =
            new OrderStateProjector();

    replayEngine.replay(firstProjector);
    replayEngine.replay(secondProjector);

    OrderState firstState =
            firstProjector.getState("ORD-001");

    OrderState secondState =
            secondProjector.getState("ORD-001");

    assertEquals(firstState, secondState);
}

}