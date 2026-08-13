package com.chronon.store;

import com.chronon.event.Event;

import java.util.List;

public interface EventStore {

    void append(Event event);

    List<Event> getAll();

    List<Event> getRange(long fromSequence, long toSequence);

    long size();
}