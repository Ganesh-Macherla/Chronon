package com.chronon.store;

import com.chronon.event.Event;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventStore implements EventStore {

    private final List<Event> events = new ArrayList<>();

    @Override
    public void append(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (!events.isEmpty()) {
            long lastSequence = events.get(events.size() - 1).sequence();

            if (event.sequence() <= lastSequence) {
                throw new IllegalArgumentException(
                        "Event sequence must be strictly increasing"
                );
            }
        }

        events.add(event);
    }

    @Override
    public List<Event> getAll() {
        return List.copyOf(events);
    }

    @Override
    public List<Event> getRange(long fromSequence, long toSequence) {

        if (fromSequence > toSequence) {
            throw new IllegalArgumentException(
                    "From sequence cannot be greater than to sequence"
            );
        }

        return events.stream()
                .filter(event ->
                        event.sequence() >= fromSequence &&
                        event.sequence() <= toSequence
                )
                .toList();
    }

    @Override
    public long size() {
        return events.size();
    }
}