package com.chronon.strategy;

import com.chronon.event.PriceUpdate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class MovingAverageStrategy implements Strategy {

    private final int shortWindow;
    private final int longWindow;

    private final Deque<Double> prices = new ArrayDeque<>();

    public MovingAverageStrategy(int shortWindow, int longWindow) {

        if (shortWindow <= 0 || longWindow <= 0) {
            throw new IllegalArgumentException(
                    "Moving average windows must be positive"
            );
        }

        if (shortWindow >= longWindow) {
            throw new IllegalArgumentException(
                    "Short window must be smaller than long window"
            );
        }

        this.shortWindow = shortWindow;
        this.longWindow = longWindow;
    }

    @Override
    public Decision onPriceUpdate(PriceUpdate priceUpdate) {

        prices.addLast(priceUpdate.price().doubleValue());

        if (prices.size() > longWindow) {
            prices.removeFirst();
        }

        if (prices.size() < longWindow) {
            return new Decision(
                    Action.HOLD,
                    Map.of(
                            "pricesCollected", prices.size(),
                            "requiredPrices", longWindow
                    ),
                    "Not enough price history to calculate the moving averages.",
                    0.0
            );
        }

        double shortAverage = calculateAverage(shortWindow);
        double longAverage = calculateAverage(longWindow);

        Action action;

        if (shortAverage > longAverage) {
            action = Action.BUY;
        } else if (shortAverage < longAverage) {
            action = Action.SELL;
        } else {
            action = Action.HOLD;
        }

        return new Decision(
                action,
                Map.of(
                        "shortMA", shortAverage,
                        "longMA", longAverage
                ),
                "Short moving average compared against long moving average.",
                0.5
        );
    }

    private double calculateAverage(int window) {

        return prices.stream()
                .skip(prices.size() - window)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}