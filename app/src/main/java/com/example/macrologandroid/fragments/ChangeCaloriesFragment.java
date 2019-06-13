package com.example.macrologandroid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.macrologandroid.dtos.UserSettingsResponse;
import com.example.macrologandroid.models.ChangeGoalMacros;
import com.example.macrologandroid.models.Gender;
import com.example.macrologandroid.R;

import org.jetbrains.annotations.NotNull;

public class ChangeCaloriesFragment extends Fragment implements ChangeGoalMacros {

    private TextView caloriesView;
    private TextView carbsView;

    private UserSettingsResponse userSettings;

    private double goalCalories;
    private double goalProtein;
    private double goalFat;
    private double goalCarbs;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.layout_change_calories, container, false);

        if (getArguments() != null) {
            userSettings = (UserSettingsResponse) getArguments().getSerializable("userSettings");
        }
        goalCalories = calculateCalories();
        goalProtein = userSettings.getWeight() * 1.8;
        goalFat = userSettings.getWeight() * 0.8;
        goalCarbs = calculateCarbs();

        caloriesView = view.findViewById(R.id.calories_output);
        caloriesView.setText(String.valueOf(Math.round(goalCalories)));

        TextView proteinView = view.findViewById(R.id.protein_output);
        proteinView.setText(String.valueOf(Math.round(goalProtein)));

        TextView fatView = view.findViewById(R.id.fat_output);
        fatView.setText(String.valueOf(Math.round(goalFat)));

        carbsView = view.findViewById(R.id.carbs_output);
        carbsView.setText(String.valueOf(Math.round(goalCarbs)));


        SeekBar slider = view.findViewById(R.id.slider);
        slider.setMax(4000);
        slider.setMin(1200);
        slider.setProgress(1960);

        slider.setOnSeekBarChangeListener(seekBarChangeListener);

        return view;

    }

    private double calculateCarbs() {
        return (goalCalories - (goalProtein * 4) - (goalFat * 9)) / 4;
    }

    private double calculateCalories() {
        double bmr;
        if (userSettings.getGender() == Gender.MALE) {
            bmr = 66.5 + (13.7 * userSettings.getWeight()) + (5 * userSettings.getHeight()) - (6.76 * userSettings.getAge());
        } else {
            bmr = 655.0 + (9.56 * userSettings.getWeight()) + (1.8 * userSettings.getHeight()) - (4.68 * userSettings.getAge());
        }
        return bmr * userSettings.getActivity();
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            goalCalories = progress;
            caloriesView.setText(String.valueOf(Math.round(goalCalories)));
            goalCarbs = calculateCarbs();
            carbsView.setText(String.valueOf(Math.round(goalCarbs)));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Override
    public Bundle getGoalMacros() {
        Bundle bundle = new Bundle();
        bundle.putInt("goalProtein", Integer.valueOf(String.valueOf(Math.round(goalProtein))));
        bundle.putInt("goalFat", Integer.valueOf(String.valueOf(Math.round(goalFat))));
        bundle.putInt("goalCarbs", Integer.valueOf(String.valueOf(Math.round(goalCarbs))));
        return bundle;

    }

}
