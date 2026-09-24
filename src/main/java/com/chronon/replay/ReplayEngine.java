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

    public <S> S reconstruct(
            S initialState,
            StateReducer<S> reducer
    ) {
        if (reducer == null) {
            throw new IllegalArgumentException(
                    "State reducer cannot be null"
            );
        }

        S state = initialState;

        for (Event event : eventStore.getAll()) {
            state = reducer.apply(state, event);
        }

        return state;
    }

    public <S> S reconstructRange(
            long fromSequence,
            long toSequence,
            S initialState,
            StateReducer<S> reducer
    ) {
        if (reducer == null) {
            throw new IllegalArgumentException(
                    "State reducer cannot be null"
            );
        }

        if (fromSequence > toSequence) {
            throw new IllegalArgumentException(
                    "From sequence cannot be greater than to sequence"
            );
        }

        S state = initialState;

        for (Event event :
                eventStore.getRange(fromSequence, toSequence)) {

            state = reducer.apply(state, event);
        }

        return state;
    }
}