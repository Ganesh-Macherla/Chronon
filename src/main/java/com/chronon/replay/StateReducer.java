package com.chronon.replay;

import com.chronon.event.Event;

public interface StateReducer<S> {

    S apply(S state, Event event);
}