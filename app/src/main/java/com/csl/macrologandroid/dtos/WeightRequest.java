package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class WeightRequest implements Serializable {

    @Expose
    @SerializedName("id")
    private final Long id;

    @Expose
    @SerializedName("weight")
    private final double weight;

    @Expose
    @SerializedName("day")
    private final String day;

    public WeightRequest(Long id, double weight, String day) {
        this.id = id;
        this.weight = weight;
        this.day = day;
    }

    public double getWeight() {
        return weight;
    }

    public String getDay() {
        return day;
    }

}
