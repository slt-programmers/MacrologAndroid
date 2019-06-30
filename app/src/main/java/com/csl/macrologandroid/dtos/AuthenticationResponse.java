package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("token")
    private String token;

    public AuthenticationResponse(String name, String token) {
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
