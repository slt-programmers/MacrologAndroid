package com.csl.macrologandroid.dtos;

import com.csl.macrologandroid.models.Gender;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class UserSettingsResponse implements Serializable {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("age")
    private int age;

    @Expose
    @SerializedName("birthday")
    private Date birthday;

    @Expose
    @SerializedName("gender")
    private Gender gender;

    @Expose
    @SerializedName("height")
    private int height;

    @Expose
    @SerializedName("currentWeight")
    private double currentWeight;

    @Expose
    @SerializedName("activity")
    private double activity;

    @Expose
    @SerializedName("goalProtein")
    private int goalProtein;

    @Expose
    @SerializedName("goalFat")
    private int goalFat;

    @Expose
    @SerializedName("goalCarbs")
    private int goalCarbs;

    public UserSettingsResponse() {
        // Non arg constructor
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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
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
        return currentWeight;
    }

    public void setWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public double getActivity() {
        return activity;
    }

    public void setActivity(double activity) {
        this.activity = activity;
    }

    public int getGoalProtein() {
        return goalProtein;
    }

    public int getGoalFat() {
        return goalFat;
    }

    public int getGoalCarbs() {
        return goalCarbs;
    }

}
