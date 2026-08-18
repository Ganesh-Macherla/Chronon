package com.chronon.strategy;

import com.chronon.event.PriceUpdate;

public interface Strategy {

    Decision onPriceUpdate(PriceUpdate priceUpdate);
}