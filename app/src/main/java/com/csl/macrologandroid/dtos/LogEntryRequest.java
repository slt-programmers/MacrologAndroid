package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LogEntryRequest implements Serializable {

    @Expose
    @SerializedName("id")
    private Long id;

    @Expose
    @SerializedName("foodId")
    private Long foodId;

    @Expose
    @SerializedName("portionId")
    private Long portionId;

    @Expose
    @SerializedName("multiplier")
    private double multiplier;

    @Expose
    @SerializedName("day")
    private String day;

    @Expose
    @SerializedName("meal")
    private String meal;

    public LogEntryRequest(Long id, Long foodId, Long portionId, double multiplier, String day, String meal) {
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

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }

    public Long getPortionId() {
        return portionId;
    }

    public void setPortionId(Long portionId) {
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
