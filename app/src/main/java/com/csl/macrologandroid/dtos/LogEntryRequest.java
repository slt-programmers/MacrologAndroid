package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LogEntryRequest implements Serializable {

    @Expose
    @SerializedName("id")
    private final Long id;

    @Expose
    @SerializedName("foodId")
    private final Long foodId;

    @Expose
    @SerializedName("portionId")
    private final Long portionId;

    @Expose
    @SerializedName("multiplier")
    private final double multiplier;

    @Expose
    @SerializedName("day")
    private final String day;

    @Expose
    @SerializedName("meal")
    private final String meal;

    public LogEntryRequest(Long id, Long foodId, Long portionId, double multiplier, String day, String meal) {
        this.id = id;
        this.foodId = foodId;
        this.portionId = portionId;
        this.multiplier = multiplier;
        this.day = day;
        this.meal = meal;
    }

}
