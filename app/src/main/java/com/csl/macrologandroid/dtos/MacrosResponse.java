package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class MacrosResponse implements Serializable {

    private double protein;
    private double fat;
    private double carbs;
    private double calories;

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getCalories() {
        return calories;
    }

}
