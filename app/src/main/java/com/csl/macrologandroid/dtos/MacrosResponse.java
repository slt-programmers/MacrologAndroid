package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class MacrosResponse implements Serializable {

    private double protein;

    private double fat;

    private double carbs;

    private double calories;

    public MacrosResponse(double protein, double fat, double carbs, double calories) {
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.calories = calories;
    }

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

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }
}
