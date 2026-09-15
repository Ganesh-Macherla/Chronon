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
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MatchingEngine implements EventListener {

    private final LiveEventPipeline pipeline;

    private final Map<String, Queue<PendingOrder>> pendingOrders =
            new HashMap<>();

    private BigDecimal latestPrice;
    private Instant latestTimestamp;
    private String latestSymbol;
    private int availableVolume;

    public MatchingEngine(LiveEventPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void onEvent(Event event) {

        if (event instanceof PriceUpdate priceUpdate) {

            latestPrice = priceUpdate.price();
            latestTimestamp = priceUpdate.timestamp();
            latestSymbol = priceUpdate.symbol();
            availableVolume = priceUpdate.volume();

            matchPendingOrders(priceUpdate.symbol());

            return;
        }

        if (!(event instanceof OrderSubmitted order)) {
            return;
        }

        if (order.orderType() != OrderType.MARKET) {
            return;
        }

        pipeline.publish(
                new OrderAccepted(
                        pipeline.nextSequence(),
                        order.timestamp(),
                        order.orderId()
                )
        );

        Queue<PendingOrder> ordersForSymbol =
                pendingOrders.computeIfAbsent(
                        order.symbol(),
                        key -> new ArrayDeque<>()
                );

        ordersForSymbol.add(
                new PendingOrder(
                        order.orderId(),
                        order.symbol(),
                        order.quantity()
                )
        );

        if (order.symbol().equals(latestSymbol)
                && latestPrice != null
                && availableVolume > 0) {

            matchPendingOrders(order.symbol());
        }
    }

    private void matchPendingOrders(String symbol) {

        Queue<PendingOrder> orders =
                pendingOrders.get(symbol);

        if (orders == null || orders.isEmpty()) {
            return;
        }

        while (!orders.isEmpty() && availableVolume > 0) {

            PendingOrder order = orders.peek();

            int fillQuantity =
                    Math.min(
                            order.remainingQuantity(),
                            availableVolume
                    );

            order.reduce(fillQuantity);
            availableVolume -= fillQuantity;

            if (order.remainingQuantity() == 0) {

                pipeline.publish(
                        new OrderFilled(
                                pipeline.nextSequence(),
                                latestTimestamp,
                                order.orderId(),
                                fillQuantity,
                                latestPrice
                        )
                );

                orders.remove();

            } else {

                pipeline.publish(
                        new OrderPartiallyFilled(
                                pipeline.nextSequence(),
                                latestTimestamp,
                                order.orderId(),
                                fillQuantity,
                                latestPrice
                        )
                );

                break;
            }
        }

        if (orders.isEmpty()) {
            pendingOrders.remove(symbol);
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

        private int remainingQuantity() {
            return remainingQuantity;
        }

        private void reduce(int quantity) {
            remainingQuantity -= quantity;
        }
    }
}