package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class IngredientResponse implements Serializable {

    private Double multiplier;
    private FoodResponse food;
    private Long portionId;

    public IngredientResponse(Double multiplier, FoodResponse food, Long portionId) {
        this.multiplier = multiplier;
        this.food = food;
        this.portionId = portionId;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public FoodResponse getFood() {
        return food;
    }

    public void setFood(FoodResponse food) {
        this.food = food;
    }

    public Long getPortionId() {
        return portionId;
    }

    public void setPortionId(Long portionId) {
        this.portionId = portionId;
    }
}
