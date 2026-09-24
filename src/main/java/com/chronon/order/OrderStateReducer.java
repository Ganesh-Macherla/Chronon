package com.chronon.order;

import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderRejected;
import com.chronon.event.OrderSubmitted;
import com.chronon.replay.StateReducer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class OrderStateReducer
        implements StateReducer<Map<String, OrderState>> {

    @Override
    public Map<String, OrderState> apply(
            Map<String, OrderState> states,
            Event event
    ) {

        Map<String, OrderState> updatedStates =
                new HashMap<>(states);

        if (event instanceof OrderSubmitted submitted) {

            updatedStates.put(
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

            return updatedStates;
        }

        if (event instanceof OrderAccepted accepted) {

            OrderState current =
                    updatedStates.get(accepted.orderId());

            if (current == null) {
                return updatedStates;
            }

            updatedStates.put(
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

            return updatedStates;
        }

        if (event instanceof OrderPartiallyFilled partial) {

            return applyFill(
                    updatedStates,
                    partial.orderId(),
                    partial.filledQuantity(),
                    partial.fillPrice(),
                    OrderStatus.PARTIALLY_FILLED
            );
        }

        if (event instanceof OrderFilled filled) {

            return applyFill(
                    updatedStates,
                    filled.orderId(),
                    filled.filledQuantity(),
                    filled.fillPrice(),
                    OrderStatus.FILLED
            );
        }

        if (event instanceof OrderRejected rejected) {

            OrderState current =
                    updatedStates.get(rejected.orderId());

            if (current == null) {
                return updatedStates;
            }

            updatedStates.put(
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

        return updatedStates;
    }

    private Map<String, OrderState> applyFill(
            Map<String, OrderState> states,
            String orderId,
            int fillQuantity,
            BigDecimal fillPrice,
            OrderStatus status
    ) {

        OrderState current = states.get(orderId);

        if (current == null) {
            return states;
        }

        int oldFilledQuantity =
                current.filledQuantity();

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
                            BigDecimal.valueOf(
                                    oldFilledQuantity
                            )
                    );

            BigDecimal newValue =
                    fillPrice.multiply(
                            BigDecimal.valueOf(
                                    fillQuantity
                            )
                    );

            newAverage =
                    oldValue
                            .add(newValue)
                            .divide(
                                    BigDecimal.valueOf(
                                            newFilledQuantity
                                    ),
                                    8,
                                    RoundingMode.HALF_UP
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

        return states;
    }
}