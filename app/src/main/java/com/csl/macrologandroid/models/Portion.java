package com.csl.macrologandroid.models;

import com.csl.macrologandroid.dtos.PortionResponse;

import java.io.Serializable;

public class Portion implements Serializable {

    private Integer id;

    private double grams;

    private String description;

    public Portion(Integer id, double grams, String description) {
        this.id = id;
        this.grams = grams;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public double getGrams() {
        return grams;
    }

    public String getDescription() {
        return description;
    }

    public static Portion fromResponse(PortionResponse res) {
        return new Portion(res.getId(), res.getGrams(), res.getDescription());
    }
}
