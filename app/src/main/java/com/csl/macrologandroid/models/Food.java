package com.csl.macrologandroid.models;

import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.PortionResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Food implements Serializable {

    private Long id;

    private String name;

    private double protein;

    private double fat;

    private double carbs;

    private List<Portion> portions;

    public Food(Long id, String name, double protein, double fat, double carbs, List<Portion> portions) {
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

    public List<Portion> getPortions() {
        return portions;
    }

    public static Food fromResponse(FoodResponse res) {
        List<Portion> portions = new ArrayList<>();
        for(PortionResponse portionResponse : res.getPortions()) {
            portions.add(Portion.fromResponse(portionResponse));
        }
        return new Food(res.getId(), res.getName(), res.getProtein(), res.getFat(), res.getCarbs(), portions);
    }

}
