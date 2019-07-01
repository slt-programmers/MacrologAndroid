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

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

}
