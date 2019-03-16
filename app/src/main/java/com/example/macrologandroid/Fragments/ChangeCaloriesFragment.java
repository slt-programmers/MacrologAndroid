package com.example.macrologandroid.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.macrologandroid.R;

public class ChangeCaloriesFragment extends Fragment {

    private View view;
    private TextView calories;
    private TextView protein;
    private TextView fat;
    private TextView carbs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        view = inflater.inflate(R.layout.layout_change_calories, container, false);

        calories = view.findViewById(R.id.calories_output);
        calories.setText("Calories 1200" );

        protein = view.findViewById(R.id.protein_output);
        protein.setText("Protein xx" );

        fat = view.findViewById(R.id.fat_output);
        fat.setText("Fat xx" );

        carbs = view.findViewById(R.id.carbs_output);
        carbs.setText("Carbs xx" );



        SeekBar slider = view.findViewById(R.id.slider);
        slider.setMax(4000);
        slider.setMin(1200);
        slider.setProgress(1960);

        slider.setOnSeekBarChangeListener(seekBarChangeListener);

        return view;

    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            calories.setText(getResources().getString(R.string.calories) + " " +String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
