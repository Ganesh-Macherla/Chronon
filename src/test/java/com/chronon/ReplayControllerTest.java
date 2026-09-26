package com.chronon;

import com.chronon.event.Event;
import com.chronon.event.PriceUpdate;
import com.chronon.replay.ReplayController;
import com.chronon.store.InMemoryEventStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ReplayControllerTest {

    @Test
    void controllerStepsThroughEventsInOrder() {

        InMemoryEventStore store = new InMemoryEventStore();

        store.append(price(1, "100"));
        store.append(price(2, "101"));
        store.append(price(3, "102"));

        ReplayController controller =
                new ReplayController(store);

        assertTrue(controller.hasNext());
        assertFalse(controller.isFinished());
        assertEquals(0, controller.currentIndex());
        assertEquals(-1, controller.currentSequence());

        Event first = controller.step();

        assertEquals(1, first.sequence());
        assertEquals(1, controller.currentIndex());
        assertEquals(1, controller.currentSequence());

        Event second = controller.step();

        assertEquals(2, second.sequence());
        assertEquals(2, controller.currentIndex());
        assertEquals(2, controller.currentSequence());

        Event third = controller.step();

        assertEquals(3, third.sequence());
        assertEquals(3, controller.currentIndex());
        assertEquals(3, controller.currentSequence());

        assertFalse(controller.hasNext());
        assertTrue(controller.isFinished());
    }

    @Test
    void controllerCanBeReset() {

        InMemoryEventStore store = new InMemoryEventStore();

        store.append(price(1, "100"));
        store.append(price(2, "101"));

        ReplayController controller =
                new ReplayController(store);

        controller.step();
        controller.step();

        assertTrue(controller.isFinished());

        controller.reset();

        assertEquals(0, controller.currentIndex());
        assertEquals(-1, controller.currentSequence());
        assertTrue(controller.hasNext());
        assertFalse(controller.isFinished());

        assertEquals(1, controller.step().sequence());
    }

    @Test
    void stepAfterEndThrowsException() {

        InMemoryEventStore store = new InMemoryEventStore();

        store.append(price(1, "100"));

        ReplayController controller =
                new ReplayController(store);

        controller.step();

        assertThrows(
                IllegalStateException.class,
                controller::step
        );
    }

    @Test
    void nullEventStoreIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplayController(null)
        );
    }

    @Test
    void emptyStoreStartsFinished() {

        InMemoryEventStore store =
                new InMemoryEventStore();

        ReplayController controller =
                new ReplayController(store);

        assertFalse(controller.hasNext());
        assertTrue(controller.isFinished());
        assertEquals(0, controller.currentIndex());
        assertEquals(-1, controller.currentSequence());

        assertThrows(
                IllegalStateException.class,
                controller::step
        );
    }

    private PriceUpdate price(
            long sequence,
            String price
    ) {
        return new PriceUpdate(
                sequence,
                Instant.parse("2026-08-13T09:30:00Z")
                        .plusSeconds(sequence),
                "AAPL",
                new BigDecimal(price),
                100
        );
    }
}