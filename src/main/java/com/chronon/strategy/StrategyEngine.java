package com.chronon.strategy;

import com.chronon.bus.EventListener;
import com.chronon.bus.LiveEventPipeline;
import com.chronon.event.Event;
import com.chronon.event.PriceUpdate;
import com.chronon.event.StrategyDecision;

public class StrategyEngine implements EventListener {

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
    }
}