package com.example.macrologandroid;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.DTO.PortionResponse;

import java.util.ArrayList;
import java.util.List;

public class EditLogEntryActivity extends AppCompatActivity {

    private LinearLayout logentryLayout;
    private List<LogEntryResponse> originalEntries;
    private List<LogEntryResponse> copyEntries;
    private EditText foodAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        logentryLayout = findViewById(R.id.logentry_layout);
        originalEntries = (List<LogEntryResponse>) getIntent().getSerializableExtra("logentries");
        copyEntries = originalEntries;

        for (LogEntryResponse entry : originalEntries) {
            ConstraintLayout logentry = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);

            TextView foodNameTextView = logentry.findViewById(R.id.food_name);
            foodNameTextView.setText(entry.getFood().getName());

            ImageView trashImageView = logentry.findViewById(R.id.trash_icon);
            trashImageView.setOnClickListener((v) -> {
                originalEntries.remove(entry);
            });

            Spinner foodPortion = logentry.findViewById(R.id.portion_spinner);
            setupSpinner(foodPortion, entry);

            EditText foodAmount = logentry.findViewById(R.id.food_amount);
            foodAmount.setId(R.id.food_amount);

            if (entry.getPortion() == null) {
                foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                foodAmount.setText(String.valueOf(entry.getMultiplier() * 100));
            } else {
                foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                foodAmount.setText(String.valueOf(entry.getMultiplier()));
            }

            logentryLayout.addView(logentry);
        }

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveLogEntries();
        });
    }

    private void saveLogEntries() {

    }

    private void setupSpinner(Spinner foodPortion, LogEntryResponse entry) {
        List<String> list = new ArrayList<>();
        List<PortionResponse> allPortions = entry.getFood().getPortions();
        for (PortionResponse portion : allPortions) {
            list.add(portion.getDescription());
        }
        list.add("gram");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodPortion.setAdapter(dataAdapter);
        PortionResponse selectedPortion = entry.getPortion();
        if (selectedPortion != null) {
             foodPortion.setSelection(list.indexOf(selectedPortion.getDescription()));
        } else {
            foodPortion.setSelection(list.size() - 1);
        }

//        foodPortion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (((AppCompatTextView)view).getText().toString().equals("gram")) {
//                    foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    foodAmount.setText(String.valueOf(entry.getMultiplier() * 100));
//                } else {
//                    foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
//                    foodAmount.setText(String.valueOf(entry.getMultiplier()));
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) { }
//        });
    }

}
