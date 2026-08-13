package com.chronon;

import com.chronon.event.PriceUpdate;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventStoreTest {

    @Test
    void eventsAreStoredInOrder() {

        InMemoryEventStore store = new InMemoryEventStore();

        PriceUpdate first = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        PriceUpdate second = new PriceUpdate(
                2,
                Instant.parse("2026-08-13T09:30:01Z"),
                "AAPL",
                new BigDecimal("210.45"),
                300
        );

        store.append(first);
        store.append(second);

        List<com.chronon.event.Event> events = store.getAll();

        assertEquals(2, events.size());
        assertEquals(1, events.get(0).sequence());
        assertEquals(2, events.get(1).sequence());
    }

    @Test
    void duplicateSequenceIsRejected() {

        InMemoryEventStore store = new InMemoryEventStore();

        PriceUpdate first = new PriceUpdate(
                1,
                Instant.now(),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        PriceUpdate duplicate = new PriceUpdate(
                1,
                Instant.now(),
                "AAPL",
                new BigDecimal("210.45"),
                300
        );

        store.append(first);

        assertThrows(
                IllegalArgumentException.class,
                () -> store.append(duplicate)
        );
    }

    @Test
    void rangeReturnsOnlyRequestedEvents() {

        InMemoryEventStore store = new InMemoryEventStore();

        for (int i = 1; i <= 5; i++) {
            store.append(
                    new PriceUpdate(
                            i,
                            Instant.parse(
                                    "2026-08-13T09:30:00Z"
                            ).plusSeconds(i),
                            "AAPL",
                            new BigDecimal("210.40"),
                            100
                    )
            );
        }

        List<com.chronon.event.Event> range =
                store.getRange(2, 4);

        assertEquals(3, range.size());
        assertEquals(2, range.get(0).sequence());
        assertEquals(4, range.get(2).sequence());
    }
}