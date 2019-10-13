package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SettingsResponse implements Serializable {

    @Expose
    @SerializedName("id")
    private final Integer id;

    @Expose
    @SerializedName("name")
    private final String name;

    @Expose
    @SerializedName("value")
    private final String value;

    public SettingsResponse(Integer id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

}
