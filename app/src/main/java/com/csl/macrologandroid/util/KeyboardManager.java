package com.csl.macrologandroid.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardManager {

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            View view = activity.findViewById(android.R.id.content);
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}


