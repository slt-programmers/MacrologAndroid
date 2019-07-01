package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthenticationRequest {

    @Expose
    @SerializedName("username")
    private final String username;

    @Expose
    @SerializedName("email")
    private final String email;

    @Expose
    @SerializedName("password")
    private final String password;

    public AuthenticationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

}
