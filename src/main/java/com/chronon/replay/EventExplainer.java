package com.chronon.replay;

import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderRejected;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.event.StrategyDecision;

public class EventExplainer {

    public String explain(Event event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Event cannot be null"
            );
        }

        if (event instanceof PriceUpdate price) {
            return String.format(
                    "Price update: %s traded at %s with volume %d.",
                    price.symbol(),
                    price.price(),
                    price.volume()
            );
        }

        if (event instanceof OrderSubmitted order) {
            return String.format(
                    "Order %s was submitted: %s %d %s.",
                    order.orderId(),
                    order.side(),
                    order.quantity(),
                    order.symbol()
            );
        }

        if (event instanceof OrderAccepted order) {
            return String.format(
                    "Order %s was accepted.",
                    order.orderId()
            );
        }

        if (event instanceof OrderPartiallyFilled order) {
            return String.format(
                    "Order %s was partially filled: %d units at %s.",
                    order.orderId(),
                    order.filledQuantity(),
                    order.fillPrice()
            );
        }

        if (event instanceof OrderFilled order) {
            return String.format(
                    "Order %s was completely filled: %d units at %s.",
                    order.orderId(),
                    order.filledQuantity(),
                    order.fillPrice()
            );
        }

        if (event instanceof OrderRejected order) {
            return String.format(
                    "Order %s was rejected: %s.",
                    order.orderId(),
                    order.reason()
            );
        }

        if (event instanceof StrategyDecision decision) {
            return String.format(
                    "Strategy %s produced a %s decision with confidence %.2f.",
                    decision.strategyId(),
                    decision.action(),
                    decision.confidence()
            );
        }

        return "Unknown event.";
    }
}