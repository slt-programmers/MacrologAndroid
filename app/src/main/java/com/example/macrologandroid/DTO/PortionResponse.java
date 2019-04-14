package com.example.macrologandroid.DTO;

import java.io.Serializable;

public class PortionResponse implements Serializable {

    private int id;
    private double grams;
    private String description;
    private MacrosResponse macros;

    public PortionResponse(int id, double grams, String description, MacrosResponse macros) {
        this.id = id;
        this.grams = grams;
        this.description = description;
        this.macros = macros;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
