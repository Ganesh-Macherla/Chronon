package com.chronon.matching;

import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderPartiallyFilled;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.order.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MatchingEngine implements EventListener {

    private final LiveEventPipeline pipeline;

    private final Map<String, PendingOrder> pendingOrders =
            new HashMap<>();

    private BigDecimal latestPrice;
    private Instant latestTimestamp;
    private int latestVolume;

    public MatchingEngine(LiveEventPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void onEvent(Event event) {

        if (event instanceof PriceUpdate priceUpdate) {

            latestPrice = priceUpdate.price();
            latestTimestamp = priceUpdate.timestamp();
            latestVolume = priceUpdate.volume();

            matchPendingOrders(priceUpdate);

            return;
        }

        if (!(event instanceof OrderSubmitted order)) {
            return;
        }

        if (order.orderType() != OrderType.MARKET) {
            return;
        }

        if (latestPrice == null || latestVolume <= 0) {
            return;
        }

        pipeline.publish(
                new OrderAccepted(
                        pipeline.nextSequence(),
                        order.timestamp(),
                        order.orderId()
                )
        );

        PendingOrder pendingOrder =
                new PendingOrder(
                        order.orderId(),
                        order.symbol(),
                        order.quantity()
                );

        pendingOrders.put(
                order.orderId(),
                pendingOrder
        );

        matchOrder(
                pendingOrder,
                latestPrice,
                latestTimestamp,
                latestVolume
        );
    }

    private void matchPendingOrders(PriceUpdate priceUpdate) {

        if (priceUpdate.volume() <= 0) {
            return;
        }

        for (PendingOrder order :
                pendingOrders.values().toArray(new PendingOrder[0])) {

            if (!order.symbol().equals(priceUpdate.symbol())) {
                continue;
            }

            matchOrder(
                    order,
                    priceUpdate.price(),
                    priceUpdate.timestamp(),
                    priceUpdate.volume()
            );
        }
    }

    private void matchOrder(
            PendingOrder order,
            BigDecimal price,
            Instant timestamp,
            int availableVolume
    ) {

        if (order.remainingQuantity() <= 0) {
            pendingOrders.remove(order.orderId());
            return;
        }

        int fillQuantity =
                Math.min(
                        order.remainingQuantity(),
                        availableVolume
                );

        order.reduce(fillQuantity);

        if (order.remainingQuantity() == 0) {

            pipeline.publish(
                    new OrderFilled(
                            pipeline.nextSequence(),
                            timestamp,
                            order.orderId(),
                            fillQuantity,
                            price
                    )
            );

            pendingOrders.remove(order.orderId());

        } else {

            pipeline.publish(
                    new OrderPartiallyFilled(
                            pipeline.nextSequence(),
                            timestamp,
                            order.orderId(),
                            fillQuantity,
                            price
                    )
            );
        }
    }

    private static class PendingOrder {

        private final String orderId;
        private final String symbol;
        private int remainingQuantity;

        private PendingOrder(
                String orderId,
                String symbol,
                int remainingQuantity
        ) {
            this.orderId = orderId;
            this.symbol = symbol;
            this.remainingQuantity = remainingQuantity;
        }

        private String orderId() {
            return orderId;
        }

        private String symbol() {
            return symbol;
        }

        private int remainingQuantity() {
            return remainingQuantity;
        }

        private void reduce(int quantity) {
            remainingQuantity -= quantity;
        }
    }
}