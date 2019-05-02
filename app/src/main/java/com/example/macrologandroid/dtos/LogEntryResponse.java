package com.example.macrologandroid.dtos;

import com.example.macrologandroid.models.Meal;

import java.io.Serializable;
import java.util.Date;

public class LogEntryResponse implements Serializable {

    private int id;
    private FoodResponse food;
    private PortionResponse portion;
    private MacrosResponse macrosCalculated;
    private double multiplier;
    private Date day;
    private Meal meal;

    public LogEntryResponse(int id, FoodResponse food, PortionResponse portion,
                            MacrosResponse macrosCalculated, double multiplier, Date day, Meal meal) {
        this.id = id;
        this.food = food;
        this.portion = portion;
        this.macrosCalculated = macrosCalculated;
        this.multiplier = multiplier;
        this.day = day;
        this.meal = meal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public MacrosResponse getMacrosCalculated() {
        return macrosCalculated;
    }

    public void setMacrosCalculated(MacrosResponse macrosCalculated) {
        this.macrosCalculated = macrosCalculated;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }
}
