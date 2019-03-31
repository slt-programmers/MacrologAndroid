package com.example.macrologandroid.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LogEntryRequest {

    @Expose
    @SerializedName("id")
    private Long id;

    @Expose
    @SerializedName("foodId")
    private int foodId;

    @Expose
    @SerializedName("portionId")
    private int portionId;

    @Expose
    @SerializedName("multiplier")
    private double multiplier;

    @Expose
    @SerializedName("day")
    private String day;

    @Expose
    @SerializedName("meal")
    private String meal;

    public LogEntryRequest(Long id, int foodId, int portionId, double multiplier, String day, String meal) {
        this.id = id;
        this.foodId = foodId;
        this.portionId = portionId;
        this.multiplier = multiplier;
        this.day = day;
        this.meal = meal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getPortionId() {
        return portionId;
    }

    public void setPortionId(int portionId) {
        this.portionId = portionId;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }
}
