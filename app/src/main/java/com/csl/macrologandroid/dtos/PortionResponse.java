package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class PortionResponse implements Serializable {

    private Integer id;
    private double grams;
    private String description;
    private MacrosResponse macros;

    public PortionResponse(Integer id, double grams, String description, MacrosResponse macros) {
        this.id = id;
        this.grams = grams;
        this.description = description;
        this.macros = macros;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getGrams() {
        return grams;
    }

    public void setGrams(double grams) {
        this.grams = grams;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MacrosResponse getMacros() {
        return macros;
    }

    public void setMacros(MacrosResponse macros) {
        this.macros = macros;
    }
}
