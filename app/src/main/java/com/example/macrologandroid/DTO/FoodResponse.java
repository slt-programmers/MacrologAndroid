package com.example.macrologandroid.DTO;

import com.example.macrologandroid.Models.MeasurementUnit;

import java.util.List;

public class FoodResponse {

    private int id;
    private String name;
    private MeasurementUnit measurementUnit;
    private String unitName;
    private int unitGrams;
    private double protein;
    private double fat;
    private double carbs;
    private List<PortionResponse> portions;

    public FoodResponse(int id, String name, MeasurementUnit measurementUnit, String unitName, int unitGrams, double protein, double fat, double carbs, List<PortionResponse> portions) {
        this.id = id;
        this.name = name;
        this.measurementUnit = measurementUnit;
        this.unitName = unitName;
        this.unitGrams = unitGrams;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.portions = portions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public int getUnitGrams() {
        return unitGrams;
    }

    public void setUnitGrams(int unitGrams) {
        this.unitGrams = unitGrams;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public List<PortionResponse> getPortions() {
        return portions;
    }

    public void setPortions(List<PortionResponse> portions) {
        this.portions = portions;
    }
}
