package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class IngredientResponse implements Serializable {

    private Double multiplier;
    private FoodResponse food;
    private PortionResponse portion;

    public IngredientResponse(Double multiplier, FoodResponse food,  PortionResponse portion) {
        this.multiplier = multiplier;
        this.food = food;
        this.portion = portion;
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

    public PortionResponse getPortion() {
        return portion;
    }

    public void setPortion(PortionResponse portion) {
        this.portion = portion;
    }
}
