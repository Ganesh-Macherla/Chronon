package com.chronon;

import com.chronon.bus.EventBus;
import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.PriceUpdate;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LiveEventPipelineTest {

    @Test
    void eventIsStoredAndDispatched() {

        InMemoryEventStore store = new InMemoryEventStore();
        EventBus bus = new EventBus();

        List<Event> receivedEvents = new ArrayList<>();

        EventListener listener = receivedEvents::add;
        bus.subscribe(listener);

        LiveEventPipeline pipeline =
                new LiveEventPipeline(store, bus);

        PriceUpdate event = new PriceUpdate(
                1,
                Instant.parse("2026-08-13T09:30:00Z"),
                "AAPL",
                new BigDecimal("210.40"),
                500
        );

        pipeline.publish(event);

        // Event was persisted.
        assertEquals(1, store.size());
        assertEquals(event, store.getAll().get(0));

        // Event was dispatched.
        assertEquals(1, receivedEvents.size());
        assertEquals(event, receivedEvents.get(0));
    }
}