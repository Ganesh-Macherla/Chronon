package com.chronon;

import com.chronon.bus.EventBus;
import com.chronon.bus.EventListener;
import com.chronon.event.Event;
import com.chronon.event.PriceUpdate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    @Test
    void publishedEventReachesListener() {

        EventBus bus = new EventBus();

        List<Event> receivedEvents = new ArrayList<>();

        EventListener listener = receivedEvents::add;

        bus.subscribe(listener);

        PriceUpdate event = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        bus.publish(event);

        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.get(0));
    }

    @Test
    void eventReachesMultipleListeners() {

        EventBus bus = new EventBus();

        List<Event> firstListenerEvents = new ArrayList<>();
        List<Event> secondListenerEvents = new ArrayList<>();

        bus.subscribe(firstListenerEvents::add);
        bus.subscribe(secondListenerEvents::add);

        PriceUpdate event = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        bus.publish(event);

        assertEquals(1, firstListenerEvents.size());
        assertEquals(1, secondListenerEvents.size());

        assertEquals(event, firstListenerEvents.get(0));
        assertEquals(event, secondListenerEvents.get(0));
    }

    @Test
    void nullEventsAreRejected() {

        EventBus bus = new EventBus();

        assertThrows(
                IllegalArgumentException.class,
                () -> bus.publish(null)
        );
    }

    @Test
    void nestedEventsAreProcessedImmediately() {

        EventBus bus = new EventBus();

        List<String> order = new ArrayList<>();

        EventListener secondListener = event ->
                order.add("second:" + event.sequence());

        EventListener firstListener = event -> {
            order.add("first:" + event.sequence());

            if (event.sequence() == 1) {
                bus.publish(
                        new PriceUpdate(
                                2,
                                event.timestamp(),
                                "AAPL",
                                new BigDecimal("211.40"),
                              500
                        )
                );
            }
        };

        bus.subscribe(firstListener);
        bus.subscribe(secondListener);

        PriceUpdate firstEvent = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        bus.publish(firstEvent);

        assertEquals(
                List.of("first:1", "first:2", "second:2", "second:1"),
                order
        );
    }
}