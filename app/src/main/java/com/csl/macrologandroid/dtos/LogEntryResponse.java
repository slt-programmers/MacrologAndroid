package com.csl.macrologandroid.dtos;

import com.csl.macrologandroid.models.Meal;

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

    public int getId() {
        return id;
    }

    public FoodResponse getFood() {
        return food;
    }

    public PortionResponse getPortion() {
        return portion;
    }

    public MacrosResponse getMacrosCalculated() {
        return macrosCalculated;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Date getDay() {
        return day;
    }

    public Meal getMeal() {
        return meal;
    }

}
