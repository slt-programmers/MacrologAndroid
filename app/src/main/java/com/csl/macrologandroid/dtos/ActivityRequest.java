package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ActivityRequest implements Serializable {

    @Expose
    @SerializedName("id")
    private final Long id;

    @Expose
    @SerializedName("day")
    private final String day;

    @Expose
    @SerializedName("name")
    private final String name;

    @Expose
    @SerializedName("calories")
    private final Integer calories;

    @Expose
    @SerializedName("syncedWith")
    private final String syncedWith;

    @Expose
    @SerializedName("syncedId")
    private final Long syncedId;

    public ActivityRequest(Long id, String day, String name, Integer calories, String syncedWith, Long syncedId) {
        this.id = id;
        this.day = day;
        this.name = name;
        this.calories = calories;
        this.syncedWith = syncedWith;
        this.syncedId = syncedId;
    }
}
