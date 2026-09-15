package com.chronon;

import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderRejected;
import com.chronon.event.OrderSubmitted;
import com.chronon.order.OrderState;
import com.chronon.order.OrderStateProjector;
import com.chronon.order.OrderStatus;
import com.chronon.order.OrderType;
import com.chronon.order.Side;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateProjectorTest {

    private final Instant time =
            Instant.parse("2026-08-13T09:30:00Z");

    @Test
    void submittedOrderCreatesSubmittedState() {

        OrderStateProjector projector =
                new OrderStateProjector();

        projector.onEvent(
                new OrderSubmitted(
                        1,
                        time,
                        "ORD-001",
                        "AAPL",
                        Side.BUY,
                        100,
                        OrderType.MARKET,
                        null
                )
        );

        OrderState state =
                projector.getState("ORD-001");

        assertNotNull(state);
        assertEquals("ORD-001", state.orderId());
        assertEquals("AAPL", state.symbol());
        assertEquals(100, state.quantity());
        assertEquals(OrderStatus.SUBMITTED, state.status());
        assertEquals(0, state.filledQuantity());
        assertEquals(100, state.remainingQuantity());
        assertNull(state.averageFillPrice());
    }

    @Test
    void acceptedOrderUpdatesStatus() {

        OrderStateProjector projector =
                new OrderStateProjector();

        submit(projector);

        projector.onEvent(
                new OrderAccepted(
                        2,
                        time,
                        "ORD-001"
                )
        );

        OrderState state =
                projector.getState("ORD-001");

        assertEquals(
                OrderStatus.ACCEPTED,
                state.status()
        );
    }

    @Test
    void filledOrderUpdatesQuantityAndPrice() {

        OrderStateProjector projector =
                new OrderStateProjector();

        submit(projector);

        projector.onEvent(
                new OrderFilled(
                        2,
                        time,
                        "ORD-001",
                        100,
                        new BigDecimal("104.00")
                )
        );

        OrderState state =
                projector.getState("ORD-001");

        assertEquals(OrderStatus.FILLED, state.status());
        assertEquals(100, state.filledQuantity());
        assertEquals(0, state.remainingQuantity());
        assertEquals(
                new BigDecimal("104.00"),
                state.averageFillPrice()
        );
    }

    @Test
    void partialFillsUpdateAveragePrice() {

        OrderStateProjector projector =
                new OrderStateProjector();

        submit(projector);

        projector.onEvent(
                new OrderPartiallyFilled(
                        2,
                        time,
                        "ORD-001",
                        40,
                        new BigDecimal("100.00")
                )
        );

        projector.onEvent(
                new OrderFilled(
                        3,
                        time,
                        "ORD-001",
                        60,
                        new BigDecimal("110.00")
                )
        );

        OrderState state =
                projector.getState("ORD-001");

        assertEquals(OrderStatus.FILLED, state.status());
        assertEquals(100, state.filledQuantity());
        assertEquals(0, state.remainingQuantity());

        assertEquals(
                new BigDecimal("106.00000000"),
                state.averageFillPrice()
        );
    }

    @Test
    void rejectedOrderUpdatesStatus() {

        OrderStateProjector projector =
                new OrderStateProjector();

        submit(projector);

        projector.onEvent(
                new OrderRejected(
                        2,
                        time,
                        "ORD-001",
                        "Insufficient funds"
                )
        );

        OrderState state =
                projector.getState("ORD-001");

        assertEquals(
                OrderStatus.REJECTED,
                state.status()
        );

        assertEquals(0, state.filledQuantity());
    }

    private void submit(OrderStateProjector projector) {

        projector.onEvent(
                new OrderSubmitted(
                        1,
                        time,
                        "ORD-001",
                        "AAPL",
                        Side.BUY,
                        100,
                        OrderType.MARKET,
                        null
                )
        );
    }
}