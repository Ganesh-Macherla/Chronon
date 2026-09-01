package com.chronon.replay;

import com.chronon.bus.EventListener;
import com.chronon.event.Event;
import com.chronon.store.EventStore;

public class ReplayEngine {

    private final EventStore eventStore;

    public ReplayEngine(EventStore eventStore) {
        if (eventStore == null) {
            throw new IllegalArgumentException(
                    "Event store cannot be null"
            );
        } // ReplayEngine cannot function without an EventStore

        this.eventStore = eventStore;
    }

    public void replay(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                    "Listener cannot be null"
            );
        }

        for (Event event : eventStore.getAll()) {
            listener.onEvent(event);
        }
    }
}