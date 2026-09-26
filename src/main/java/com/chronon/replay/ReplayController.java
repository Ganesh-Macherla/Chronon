package com.chronon.replay;

import com.chronon.event.Event;
import com.chronon.store.EventStore;

import java.util.List;

public class ReplayController {

    private final List<Event> events;
    private int currentIndex;

    public ReplayController(EventStore eventStore) {
        if (eventStore == null) {
            throw new IllegalArgumentException(
                    "Event store cannot be null"
            );
        }

        this.events = eventStore.getAll();
        this.currentIndex = 0;
    }

    public boolean hasNext() {
        return currentIndex < events.size();
    }

    public Event step() {
        if (!hasNext()) {
            throw new IllegalStateException(
                    "No more events to replay"
            );
        }

        return events.get(currentIndex++);
    }

    public void reset() {
        currentIndex = 0;
    }

    public int currentIndex() {
        return currentIndex;
    }

    public long currentSequence() {
        if (currentIndex == 0) {
            return -1;
        }

        return events.get(currentIndex - 1).sequence();
    }

    public boolean isFinished() {
        return !hasNext();
    }
}