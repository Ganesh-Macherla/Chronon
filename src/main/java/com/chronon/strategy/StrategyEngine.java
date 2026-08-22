package com.chronon.strategy;

import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.OrderSubmitted;
import com.chronon.event.PriceUpdate;
import com.chronon.event.StrategyDecision;
import com.chronon.order.OrderType;
import com.chronon.order.Side;

public class StrategyEngine implements EventListener {

    private static final int ORDER_QUANTITY = 100;

    private final Strategy strategy;
    private final LiveEventPipeline pipeline;
    private final String strategyId;

    public StrategyEngine(
            String strategyId,
            Strategy strategy,
            LiveEventPipeline pipeline
    ) {
        this.strategyId = strategyId;
        this.strategy = strategy;
        this.pipeline = pipeline;
    }

    @Override
    public void onEvent(Event event) {

        if (!(event instanceof PriceUpdate priceUpdate)) {
            return;
        }

        Decision decision = strategy.onPriceUpdate(priceUpdate);

        // Every strategy decision becomes a permanent Chronon event.
        StrategyDecision strategyDecision =
                new StrategyDecision(
                        pipeline.nextSequence(),
                        priceUpdate.timestamp(),
                        strategyId,
                        decision.action(),
                        decision.signals(),
                        decision.reasoning(),
                        decision.confidence()
                );

        pipeline.publish(strategyDecision);

        // HOLD means there is no order to submit.
        if (decision.action() == Action.HOLD) {
            return;
        }

        Side side = decision.action() == Action.BUY
                ? Side.BUY
                : Side.SELL;

        String orderId =
                strategyId + "-order-" + strategyDecision.sequence();

        OrderSubmitted orderSubmitted =
                new OrderSubmitted(
                        pipeline.nextSequence(),
                        priceUpdate.timestamp(),
                        orderId,
                        priceUpdate.symbol(),
                        side,
                        ORDER_QUANTITY,
                        OrderType.MARKET,
                        null
                );

        pipeline.publish(orderSubmitted);
    }
}