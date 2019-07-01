package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class WeightRequest implements Serializable {

    @Expose
    @SerializedName("id")
    private Long id;

    @Expose
    @SerializedName("weight")
    private double weight;

    @Expose
    @SerializedName("day")
    private Date day;

    public WeightRequest(Long id, double weight, Date day) {
        this.id = id;
        this.weight = weight;
        this.day = day;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }
}
