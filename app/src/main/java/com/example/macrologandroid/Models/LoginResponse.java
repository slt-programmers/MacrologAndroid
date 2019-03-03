package com.example.macrologandroid.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("token")
    @Expose
    private String token;

    public LoginResponse(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
