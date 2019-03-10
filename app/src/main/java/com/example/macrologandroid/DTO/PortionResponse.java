package com.example.macrologandroid.DTO;

public class PortionResponse {

    private int id;
    private int grams;
    private String description;
    private MacrosResponse macros;

    public PortionResponse(int id, int grams, String description, MacrosResponse macros) {
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

    public int getGrams() {
        return grams;
    }

    public void setGrams(int grams) {
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
