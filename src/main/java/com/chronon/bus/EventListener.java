package com.chronon.bus;

import com.chronon.event.Event;

public interface EventListener {

    void onEvent(Event event);
}