package com.chronon.order;

import com.chronon.bus.EventListener;
import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderRejected;
import com.chronon.event.OrderSubmitted;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OrderStateProjector implements EventListener {

    private final Map<String, OrderState> states = new HashMap<>();

    @Override
    public void onEvent(Event event) {

        if (event instanceof OrderSubmitted submitted) {

            states.put(
                    submitted.orderId(),
                    new OrderState(
                            submitted.orderId(),
                            submitted.symbol(),
                            submitted.side(),
                            submitted.quantity(),
                            submitted.orderType(),
                            OrderStatus.SUBMITTED,
                            0,
                            null
                    )
            );

            return;
        }

        if (event instanceof OrderAccepted accepted) {

            OrderState current = states.get(accepted.orderId());

            if (current == null) {
                return;
            }

            states.put(
                    accepted.orderId(),
                    new OrderState(
                            current.orderId(),
                            current.symbol(),
                            current.side(),
                            current.quantity(),
                            current.type(),
                            OrderStatus.ACCEPTED,
                            current.filledQuantity(),
                            current.averageFillPrice()
                    )
            );

            return;
        }

        if (event instanceof OrderPartiallyFilled partial) {

            applyFill(
                    partial.orderId(),
                    partial.filledQuantity(),
                    partial.fillPrice(),
                    OrderStatus.PARTIALLY_FILLED
            );

            return;
        }

        if (event instanceof OrderFilled filled) {

            applyFill(
                    filled.orderId(),
                    filled.filledQuantity(),
                    filled.fillPrice(),
                    OrderStatus.FILLED
            );

            return;
        }

        if (event instanceof OrderRejected rejected) {

            OrderState current = states.get(rejected.orderId());

            if (current == null) {
                return;
            }

            states.put(
                    rejected.orderId(),
                    new OrderState(
                            current.orderId(),
                            current.symbol(),
                            current.side(),
                            current.quantity(),
                            current.type(),
                            OrderStatus.REJECTED,
                            current.filledQuantity(),
                            current.averageFillPrice()
                    )
            );
        }
    }

    private void applyFill(
            String orderId,
            int fillQuantity,
            BigDecimal fillPrice,
            OrderStatus status
    ) {

        OrderState current = states.get(orderId);

        if (current == null) {
            return;
        }

        int oldFilledQuantity = current.filledQuantity();
        int newFilledQuantity =
                oldFilledQuantity + fillQuantity;

        BigDecimal oldAverage =
                current.averageFillPrice();

        BigDecimal newAverage;

        if (oldAverage == null || oldFilledQuantity == 0) {

            newAverage = fillPrice;

        } else {

            BigDecimal oldValue =
                    oldAverage.multiply(
                            BigDecimal.valueOf(oldFilledQuantity)
                    );

            BigDecimal newValue =
                    fillPrice.multiply(
                            BigDecimal.valueOf(fillQuantity)
                    );

            newAverage =
                    oldValue
                            .add(newValue)
                            .divide(
                                    BigDecimal.valueOf(newFilledQuantity),
                                    8,
                                    java.math.RoundingMode.HALF_UP
                            );
        }

        states.put(
                orderId,
                new OrderState(
                        current.orderId(),
                        current.symbol(),
                        current.side(),
                        current.quantity(),
                        current.type(),
                        status,
                        newFilledQuantity,
                        newAverage
                )
        );
    }

    public OrderState getState(String orderId) {
        return states.get(orderId);
    }

    public Map<String, OrderState> getAllStates() {
        return Map.copyOf(states);
    }
}