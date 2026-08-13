package com.chronon;

import com.chronon.clock.VirtualClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VirtualClockTest {

    @Test
    void clockStartsAtGivenTime() {
        Instant start = Instant.parse("2026-08-13T09:30:00Z");

        VirtualClock clock = new VirtualClock(start);

        assertEquals(start, clock.now());
    }

    @Test
    void clockCanAdvance() {
        Instant start = Instant.parse("2026-08-13T09:30:00Z");

        VirtualClock clock = new VirtualClock(start);

        clock.advanceBySeconds(10);

        assertEquals(
                Instant.parse("2026-08-13T09:30:10Z"),
                clock.now()
        );
    }

    @Test
    void clockCannotMoveBackwards() {
        Instant start = Instant.parse("2026-08-13T09:30:00Z");

        VirtualClock clock = new VirtualClock(start);

        assertThrows(
                IllegalArgumentException.class,
                () -> clock.advanceTo(
                        Instant.parse("2026-08-13T09:29:00Z")
                )
        );
    }
}