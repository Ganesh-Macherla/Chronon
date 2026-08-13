package com.chronon.bus;

import com.chronon.event.Event;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private final List<EventListener> listeners = new ArrayList<>();

    public void subscribe(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                    "Listener cannot be null"
            );
        }

        listeners.add(listener);
    }

    public void publish(Event event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Event cannot be null"
            );
        }

        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}