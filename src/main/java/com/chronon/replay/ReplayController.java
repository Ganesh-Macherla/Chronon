package com.chronon.replay;

import com.chronon.bus.EventListener;
import com.chronon.clock.VirtualClock;
import com.chronon.event.Event;

import java.util.List;

public class ReplayController {

    private final ReplayEngine replayEngine;
    private final VirtualClock clock;
    private final List<Event> events;

    private int currentIndex;
    private boolean paused;
    private ReplayStatus status;

    public ReplayController(
            ReplayEngine replayEngine,
            VirtualClock clock,
            List<Event> events
    ) {
        if (replayEngine == null) {
            throw new IllegalArgumentException(
                    "Replay engine cannot be null"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Virtual clock cannot be null"
            );
        }

        if (events == null) {
            throw new IllegalArgumentException(
                    "Events cannot be null"
            );
        }

        this.replayEngine = replayEngine;
        this.clock = clock;
        this.events = List.copyOf(events);
        this.currentIndex = 0;
        this.paused = false;
        this.status = ReplayStatus.READY;
    }

    public boolean hasNext() {
        return currentIndex < events.size();
    }

    public void step(EventListener listener) {

        if (listener == null) {
            throw new IllegalArgumentException(
                    "Listener cannot be null"
            );
        }

        if (!hasNext()) {
            return;
        }

        Event event = events.get(currentIndex);

        clock.advanceTo(event.timestamp());

        listener.onEvent(event);

        currentIndex++;
    }

    public void start(EventListener listener) {

        if (listener == null) {
            throw new IllegalArgumentException(
                    "Listener cannot be null"
            );
        }

        status = ReplayStatus.RUNNING;

        while (hasNext() && !paused) {
            step(listener);
        }

        if (!hasNext()) {
            status = ReplayStatus.COMPLETED;
        }
    }    

        public void pause() {
        paused = true;
        status = ReplayStatus.PAUSED;

    }
    
    public void resume() {
        paused = false;
        if (hasNext()) {
            status = ReplayStatus.RUNNING;
        }
    }

    public void reset() {
        currentIndex = 0;
    }

    public int currentIndex() {
        return currentIndex;
    }

    public ReplayStatus status() {
        return status;
    }

}