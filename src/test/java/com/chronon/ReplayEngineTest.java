package com.chronon;

import com.chronon.bus.EventListener;
import com.chronon.event.PriceUpdate;
import com.chronon.replay.ReplayEngine;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
}