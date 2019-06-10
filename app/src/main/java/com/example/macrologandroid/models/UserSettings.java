package com.example.macrologandroid.models;

import com.example.macrologandroid.dtos.SettingsResponse;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserSettings implements Serializable {

    private String name;
    private int age;
    private LocalDate birthday;
    private Gender gender;
    private int height;
    private double weight;
    private double activity;

    private int protein;
    private int fat;
    private int carbs;

    public UserSettings() {
    }

    public UserSettings(List<SettingsResponse> response) {
        try {
            this.name = mapSetting(response, "name");
            this.birthday = LocalDate.parse(mapSetting(response, "birthday"), DateTimeFormatter.ofPattern("d-M-yyyy"));
            this.age = Period.between(birthday, LocalDate.now()).getYears();
            this.gender = Gender.valueOf(mapSetting(response, "gender"));
            this.height = Integer.parseInt(mapSetting(response, "height"));
            this.weight = Double.parseDouble(mapSetting(response, "weight"));
            this.activity = Double.parseDouble(mapSetting(response, "activity"));

            this.protein = Integer.parseInt(mapSetting(response, "goalProtein"));
            this.fat = Integer.parseInt(mapSetting(response, "goalFat"));
            this.carbs = Integer.parseInt(mapSetting(response, "goalCarbs"));
        } catch (Exception e) {
            new UserSettings();
        }
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

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
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

    private String mapSetting(List<SettingsResponse> response, String identifier) {
        return response.stream().filter(s -> s.getName().equals(identifier)).findFirst()
                .orElse(new SettingsResponse(0, "", "")).getValue();

    }

}
