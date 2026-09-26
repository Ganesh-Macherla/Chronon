package com.chronon;

import com.chronon.clock.VirtualClock;
import com.chronon.event.PriceUpdate;
import com.chronon.replay.ReplayController;
import com.chronon.replay.ReplayEngine;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplayControllerTest {

    @Test
    void stepAdvancesClockAndDispatchesOneEvent() {

        InMemoryEventStore store =
                new InMemoryEventStore();

        PriceUpdate first =
                new PriceUpdate(
                        1,
                        Instant.parse("2026-08-13T09:30:00Z"),
                        "AAPL",
                        new BigDecimal("100.00"),
                        100
                );

        PriceUpdate second =
                new PriceUpdate(
                        2,
                        Instant.parse("2026-08-13T09:31:00Z"),
                        "AAPL",
                        new BigDecimal("101.00"),
                        100
                );

        store.append(first);
        store.append(second);

        VirtualClock clock =
                new VirtualClock(
                        Instant.parse("2026-08-13T09:29:00Z")
                );

        ReplayController controller =
                new ReplayController(
                        new ReplayEngine(store),
                        clock,
                        store.getAll()
                );

        List<Long> received =
                new ArrayList<>();

        controller.step(
                event -> received.add(event.sequence())
        );

        assertEquals(
                List.of(1L),
                received
        );

        assertEquals(
                first.timestamp(),
                clock.now()
        );

        assertEquals(
                1,
                controller.currentIndex()
        );

        assertTrue(controller.hasNext());
    }

    @Test
    void resetReturnsControllerToBeginning() {

        InMemoryEventStore store =
                new InMemoryEventStore();

        store.append(
                new PriceUpdate(
                        1,
                        Instant.parse("2026-08-13T09:30:00Z"),
                        "AAPL",
                        new BigDecimal("100.00"),
                        100
                )
        );

        VirtualClock clock =
                new VirtualClock(
                        Instant.parse("2026-08-13T09:29:00Z")
                );

        ReplayController controller =
                new ReplayController(
                        new ReplayEngine(store),
                        clock,
                        store.getAll()
                );

        controller.step(event -> {});

        assertEquals(1, controller.currentIndex());

        controller.reset();

        assertEquals(0, controller.currentIndex());
        assertTrue(controller.hasNext());
    }

    @Test
void startReplaysAllEvents() {

    InMemoryEventStore store =
            new InMemoryEventStore();

    store.append(
            new PriceUpdate(
                    1,
                    Instant.parse("2026-08-13T09:30:00Z"),
                    "AAPL",
                    new BigDecimal("100.00"),
                    100
            )
    );

    store.append(
            new PriceUpdate(
                    2,
                    Instant.parse("2026-08-13T09:31:00Z"),
                    "AAPL",
                    new BigDecimal("101.00"),
                    100
            )
    );

    VirtualClock clock =
            new VirtualClock(
                    Instant.parse("2026-08-13T09:29:00Z")
            );

    ReplayController controller =
            new ReplayController(
                    new ReplayEngine(store),
                    clock,
                    store.getAll()
            );

    List<Long> received =
            new ArrayList<>();

    controller.start(
            event -> received.add(event.sequence())
    );

    assertEquals(
            List.of(1L, 2L),
            received
    );

    assertEquals(
            2,
            controller.currentIndex()
    );

    assertFalse(controller.hasNext());

    assertEquals(
            Instant.parse("2026-08-13T09:31:00Z"),
            clock.now()
    );
}
}