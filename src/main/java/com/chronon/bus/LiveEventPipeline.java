package com.chronon.bus;

import com.chronon.event.Event;
import com.chronon.store.EventStore;

public class LiveEventPipeline {

    private final EventStore eventStore;
    private final EventBus eventBus;

    public LiveEventPipeline(EventStore eventStore, EventBus eventBus) {
        this.eventStore = eventStore;
        this.eventBus = eventBus;
    }

    public void publish(Event event) {
        // Persist first: the event becomes part of Chronon's history.
        eventStore.append(event);

        // Then notify live listeners.
        eventBus.publish(event);
    }
}