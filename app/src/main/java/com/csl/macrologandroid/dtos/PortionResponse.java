package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class PortionResponse implements Serializable {

    private final Integer id;
    private final double grams;
    private final String description;

    public PortionResponse(Integer id, double grams, String description) {
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

}
