package com.csl.macrologandroid.dtos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {

    @Expose
    @SerializedName("oldPassword")
    private final String oldPassword;

    @Expose
    @SerializedName("newPassword")
    private final String newPassword;

    @Expose
    @SerializedName("confirmPassword")
    private final String confirmPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

}
