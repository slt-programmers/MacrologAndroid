package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EntryDto implements Serializable {

    @Expose
    @SerializedName("id")
    private final Long id;

    @Expose
    @SerializedName("food")
    private final FoodResponse food;

    @Expose
    @SerializedName("portion")
    private final PortionResponse portion;

    @Expose
    @SerializedName("multiplier")
    private final double multiplier;

    @Expose
    @SerializedName("day")
    private final String day;

    @Expose
    @SerializedName("meal")
    private final String meal;

    public EntryDto(Long id, FoodResponse food, PortionResponse portion, double multiplier, String day, String meal) {
        this.id = id;
        this.food = food;
        this.portion = portion;
        this.multiplier = multiplier;
        this.day = day;
        this.meal = meal;
    }

}
