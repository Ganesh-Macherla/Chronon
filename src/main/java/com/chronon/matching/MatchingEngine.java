package com.chronon.matching;

import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderAccepted;
import com.chronon.event.OrderFilled;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.order.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public class MatchingEngine implements EventListener {

    private final LiveEventPipeline pipeline;

    private BigDecimal latestPrice;
    private Instant latestTimestamp;

    public MatchingEngine(LiveEventPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void onEvent(Event event) {

        if (event instanceof PriceUpdate priceUpdate) {
            latestPrice = priceUpdate.price();
            latestTimestamp = priceUpdate.timestamp();
            return;
        }

        if (!(event instanceof OrderSubmitted order)) {
            return;
        }

        if (order.orderType() != OrderType.MARKET) {
            return;
        }

        if (latestPrice == null) {
            return;
        }

        pipeline.publish(
                new OrderAccepted(
                        pipeline.nextSequence(),
                        order.timestamp(),
                        order.orderId()
                )
        );

        pipeline.publish(
                new OrderFilled(
                        pipeline.nextSequence(),
                        latestTimestamp,
                        order.orderId(),
                        order.quantity(),
                        latestPrice
                )
        );
    }
}