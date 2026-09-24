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
        }

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

    public void replayRange(
            long fromSequence,
            long toSequence,
            EventListener listener
    ) {
        if (listener == null) {
            throw new IllegalArgumentException(
                    "Listener cannot be null"
            );
        }

        if (fromSequence > toSequence) {
            throw new IllegalArgumentException(
                    "From sequence cannot be greater than to sequence"
            );
        }

        for (Event event :
                eventStore.getRange(fromSequence, toSequence)) {

            listener.onEvent(event);
        }
    }
}