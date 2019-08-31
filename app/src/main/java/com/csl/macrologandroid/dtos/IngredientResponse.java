package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class IngredientResponse implements Serializable {

    private final Double multiplier;
    private final FoodResponse food;
    private final Long portionId;

    public IngredientResponse(Double multiplier, FoodResponse food, Long portionId) {
        this.multiplier = multiplier;
        this.food = food;
        this.portionId = portionId;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public FoodResponse getFood() {
        return food;
    }

    public Long getPortionId() {
        return portionId;
    }
}
