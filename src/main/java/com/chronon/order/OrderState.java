package com.chronon.order;

import java.math.BigDecimal;

public record OrderState(
        String orderId,
        String symbol,
        Side side,
        int quantity,
        OrderType type,
        OrderStatus status,
        int filledQuantity,
        BigDecimal averageFillPrice
) {

    public int remainingQuantity() {
        return quantity - filledQuantity;
    }
}