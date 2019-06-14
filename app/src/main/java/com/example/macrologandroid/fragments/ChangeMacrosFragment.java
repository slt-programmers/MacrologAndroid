package com.example.macrologandroid.fragments;

import android.app.PendingIntent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.macrologandroid.cache.UserSettingsCache;
import com.example.macrologandroid.dtos.UserSettingsResponse;
import com.example.macrologandroid.models.ChangeGoalMacros;
import com.example.macrologandroid.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChangeMacrosFragment extends Fragment implements ChangeGoalMacros {

    private TextInputEditText proteinView;
    private TextInputEditText fatView;
    private TextInputEditText carbsView;
    private TextView caloriesView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.layout_change_macros, container, false);

        proteinView = view.findViewById(R.id.edit_protein);
        fatView = view.findViewById(R.id.edit_fat);
        carbsView = view.findViewById(R.id.edit_carbs);
        caloriesView = view.findViewById(R.id.calories_result);

        Bundle arguments = getArguments();
        if (arguments != null) {
            proteinView.setText(String.valueOf(arguments.getInt("goalProtein")));
            fatView.setText(String.valueOf(arguments.getInt("goalFat")));
            carbsView.setText(String.valueOf(arguments.getInt("goalCarbs")));
        }
        caloriesView.setText(String.valueOf(calculateCalories()));

        proteinView.addTextChangedListener(textwatcher);
        fatView.addTextChangedListener(textwatcher);
        carbsView.addTextChangedListener(textwatcher);

        return view;
    }

    private long calculateCalories() {
        double protein = Double.valueOf(handleEmptyString(Objects.requireNonNull(proteinView.getText()).toString()));
        double fat = Double.valueOf(handleEmptyString(Objects.requireNonNull(fatView.getText()).toString()));
        double carbs = Double.valueOf(handleEmptyString(Objects.requireNonNull(carbsView.getText()).toString()));

        return Math.round((protein * 4.0) + (fat * 9.0) + (carbs * 4.0));
    }

    private String handleEmptyString(String string) {
        if (string.isEmpty()) {
            return "0.0";
        }
        return string;
    }

    TextWatcher textwatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start,
        int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            caloriesView.setText(String.valueOf(calculateCalories()));
        }
    };

    public Bundle getGoalMacros() {
        Bundle bundle = new Bundle();
        bundle.putInt("goalProtein", Integer.valueOf(Objects.requireNonNull(proteinView.getText()).toString()));
        bundle.putInt("goalFat", Integer.valueOf(Objects.requireNonNull(fatView.getText()).toString()));
        bundle.putInt("goalCarbs", Integer.valueOf(Objects.requireNonNull(carbsView.getText()).toString()));
        return bundle;
    }

}
