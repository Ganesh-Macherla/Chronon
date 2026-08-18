package com.chronon.order;

public record Order(
        String orderId,
        String symbol,
        Side side,
        int quantity,
        OrderType type
) {

    public Order {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Order ID cannot be blank"
            );
        }

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol cannot be blank"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be positive"
            );
        }
    }
}