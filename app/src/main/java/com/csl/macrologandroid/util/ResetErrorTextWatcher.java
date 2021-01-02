package com.csl.macrologandroid.util;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ResetErrorTextWatcher implements TextWatcher {

    private final TextView errorText;
    private final EditText input;

    public ResetErrorTextWatcher(EditText input, TextView errorText) {
        this.input = input;
        this.errorText = errorText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        input.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        errorText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
