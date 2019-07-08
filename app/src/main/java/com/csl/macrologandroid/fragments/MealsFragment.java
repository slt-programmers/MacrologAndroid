package com.csl.macrologandroid.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csl.macrologandroid.R;

import org.jetbrains.annotations.NotNull;

public class MealsFragment extends Fragment {

    public MealsFragment() {
        // Non arg constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meals, container, false);
    }

}
