
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

        String description;

        if (event instanceof PriceUpdate price) {
            description = String.format(
                    "Price update: %s traded at %s with volume %d.",
                    price.symbol(),
                    price.price(),
                    price.volume()
            );

        } else if (event instanceof OrderSubmitted order) {
            description = String.format(
                    "Order %s was submitted: %s %d %s.",
                    order.orderId(),
                    order.side(),
                    order.quantity(),
                    order.symbol()
            );

        } else if (event instanceof OrderAccepted order) {
            description = String.format(
                    "Order %s was accepted.",
                    order.orderId()
            );

        } else if (event instanceof OrderPartiallyFilled order) {
            description = String.format(
                    "Order %s was partially filled: %d units at %s.",
                    order.orderId(),
                    order.filledQuantity(),
                    order.fillPrice()
            );

        } else if (event instanceof OrderFilled order) {
            description = String.format(
                    "Order %s was completely filled: %d units at %s.",
                    order.orderId(),
                    order.filledQuantity(),
                    order.fillPrice()
            );

        } else if (event instanceof OrderRejected order) {
            description = String.format(
                    "Order %s was rejected: %s.",
                    order.orderId(),
                    order.reason()
            );

        } else if (event instanceof StrategyDecision decision) {
            description = String.format(
                    "Strategy %s produced a %s decision with confidence %.2f.",
                    decision.strategyId(),
                    decision.action(),
                    decision.confidence()
            );

        } else {
            description = "Unknown event.";
        }

        return String.format(
                "Sequence: %d | Timestamp: %s | %s",
                event.sequence(),
                event.timestamp(),
                description
        );
    }
}
