package com.chronon;

import com.chronon.order.Order;
import com.chronon.order.OrderType;
import com.chronon.order.Side;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void createsValidOrder() {

        Order order = new Order(
                "ORD-001",
                "AAPL",
                Side.BUY,
                250,
                OrderType.MARKET
        );

        assertEquals("ORD-001", order.orderId());
        assertEquals("AAPL", order.symbol());
        assertEquals(Side.BUY, order.side());
        assertEquals(250, order.quantity());
        assertEquals(OrderType.MARKET, order.type());
    }

    @Test
    void rejectsZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        "ORD-001",
                        "AAPL",
                        Side.BUY,
                        0,
                        OrderType.MARKET
                )
        );
    }

    @Test
    void rejectsNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        "ORD-001",
                        "AAPL",
                        Side.BUY,
                        -10,
                        OrderType.MARKET
                )
        );
    }
}