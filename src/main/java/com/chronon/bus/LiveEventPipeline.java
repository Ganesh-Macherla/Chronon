package com.chronon.bus;

import com.chronon.event.Event;
import com.chronon.store.EventStore;

public class LiveEventPipeline {

    private final EventStore eventStore;
    private final EventBus eventBus;

    private long nextSequence = 1;

    public LiveEventPipeline(EventStore eventStore, EventBus eventBus) {
        this.eventStore = eventStore;
        this.eventBus = eventBus;
    }

    /**
     * Returns the sequence number that the next event should receive.
     * The number is consumed when that event is published.
     */
    public long nextSequence() {
        return nextSequence;
    }

    public void publish(Event event) {

        if (event.sequence() != nextSequence) {
            throw new IllegalArgumentException(
                    "Expected event sequence "
                            + nextSequence
                            + " but received "
                            + event.sequence()
            );
        }

        // Persist first.
        eventStore.append(event);

        // Advance the global sequence before dispatching.
        // This allows listeners to create the next event.
        nextSequence++;

        // Then notify live listeners.
        eventBus.publish(event);
    }
}