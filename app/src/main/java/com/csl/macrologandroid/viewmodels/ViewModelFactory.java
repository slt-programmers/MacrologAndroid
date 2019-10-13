package com.csl.macrologandroid.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final String token;

    public ViewModelFactory(String token) {
        this.token = token;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FoodViewModel.class)) {
            return (T) new FoodViewModel(token);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
