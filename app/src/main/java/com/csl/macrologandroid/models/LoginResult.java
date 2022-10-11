package com.csl.macrologandroid.models;

public class LoginResult {

    private final Boolean success;
    private final Throwable error;

    public LoginResult(Boolean success, Throwable error) {
        this.success = success;
        this.error = error;
    }

    public Throwable getError() {
        return this.error;
    }

    public Boolean isSuccess() {
        return this.success;
    }

}
