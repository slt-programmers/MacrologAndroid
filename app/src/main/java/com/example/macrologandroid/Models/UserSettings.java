package com.example.macrologandroid.Models;

import com.example.macrologandroid.DTO.UserSettingResponse;

import java.io.Serializable;
import java.util.List;

public class UserSettings implements Serializable {

    private String name;
    private int age;
    private Gender gender;
    private int height;
    private double weight;
    private double activity;

    private int protein;
    private int fat;
    private int carbs;

    public UserSettings() {
    }

    public UserSettings(List<UserSettingResponse> response) {
        this.name = mapSetting(response, "name");
        this.age = Integer.parseInt(mapSetting(response, "age"));
        this.gender = Gender.valueOf(mapSetting(response, "gender"));
        this.height = Integer.parseInt(mapSetting(response, "height"));
        this.weight = Double.parseDouble(mapSetting(response, "weight"));
        this.activity = Double.parseDouble(mapSetting(response, "activity"));

        this.protein = Integer.parseInt(mapSetting(response, "goalProtein"));
        this.fat = Integer.parseInt(mapSetting(response, "goalFat"));
        this.carbs = Integer.parseInt(mapSetting(response, "goalCarbs"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getActivity() {
        return activity;
    }

    public void setActivity(double activity) {
        this.activity = activity;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    private String mapSetting(List<UserSettingResponse> response, String identifier) {
        return response.stream().filter(s -> s.getName().equals(identifier)).findFirst()
                .orElse(new UserSettingResponse(0, "", "")).getValue();

    }
}
