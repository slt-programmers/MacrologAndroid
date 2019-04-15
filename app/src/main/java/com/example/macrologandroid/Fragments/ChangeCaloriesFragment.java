package com.example.macrologandroid.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.macrologandroid.Models.ChangeGoalMacros;
import com.example.macrologandroid.Models.Gender;
import com.example.macrologandroid.Models.UserSettings;
import com.example.macrologandroid.R;

public class ChangeCaloriesFragment extends Fragment implements ChangeGoalMacros {

    private TextView caloriesView;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;

    private UserSettings userSettings;

    private double goalCalories;
    private double goalProtein;
    private double goalFat;
    private double goalCarbs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.layout_change_calories, container, false);

        userSettings = (UserSettings) getArguments().getSerializable("userSettings");
        goalCalories = calculateCalories();
        goalProtein = userSettings.getWeight() * 1.8;
        goalFat = userSettings.getWeight() * 0.8;
        goalCarbs = calculateCarbs();

        caloriesView = view.findViewById(R.id.calories_output);
        caloriesView.setText(String.valueOf(Math.round(goalCalories)));

        proteinView = view.findViewById(R.id.protein_output);
        proteinView.setText(String.valueOf(Math.round(goalProtein)));

        fatView = view.findViewById(R.id.fat_output);
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
