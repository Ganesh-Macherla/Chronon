package com.chronon.clock;

import java.time.Instant;

public class VirtualClock {

    private Instant currentTime;

    public VirtualClock(Instant startTime) {
        this.currentTime = startTime;
    }

    public Instant now() {
        return currentTime;
    }

    public void advanceTo(Instant newTime) {
        if (newTime.isBefore(currentTime)) {
            throw new IllegalArgumentException(
                    "Virtual clock cannot move backwards"
            );
        }

        currentTime = newTime;
    }

    public void advanceBySeconds(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException(
                    "Cannot advance by a negative duration"
            );
        }

        currentTime = currentTime.plusSeconds(seconds);
    }
}