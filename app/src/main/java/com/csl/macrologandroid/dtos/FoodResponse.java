package com.csl.macrologandroid.dtos;

import java.io.Serializable;
import java.util.List;

public class FoodResponse implements Serializable {

    private Long id;
    private final String name;
    private final double protein;
    private final double fat;
    private final double carbs;
    private final List<PortionResponse> portions;

    public FoodResponse(Long id, String name, double protein, double fat, double carbs, List<PortionResponse> portions) {
        this.id = id;
        this.name = name;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.portions = portions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
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

    public List<PortionResponse> getPortions() {
        return portions;
    }

}
