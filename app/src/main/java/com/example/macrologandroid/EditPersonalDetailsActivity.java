package com.example.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.macrologandroid.Models.Gender;

import java.util.ArrayList;
import java.util.List;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private String originalName;
    private int originalAge;
    private Gender originalGender;
    private int originalHeight;
    private double originalWeight;
    private double originalActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        Intent intent = getIntent();
        EditText editName = findViewById(R.id.edit_name);
        originalName = intent.getStringExtra("name");
        editName.setText(originalName);

        EditText editAge = findViewById(R.id.edit_age);
        originalAge = intent.getIntExtra("age", 0);
        editAge.setText(String.valueOf(originalAge));

        RadioGroup genderRadios = findViewById(R.id.radiogroup_gender);

        originalGender = (Gender) intent.getSerializableExtra("gender");
        if (originalGender.equals(Gender.MALE)) {
            genderRadios.check(R.id.check_male);
        } else {
            genderRadios.check(R.id.check_female);
        }

        EditText editHeight = findViewById(R.id.edit_height);
        originalHeight = intent.getIntExtra("height", 0);
        editHeight.setText(String.valueOf(originalHeight));

        EditText editWeight = findViewById(R.id.edit_weight);
        originalWeight = intent.getDoubleExtra("weight", 0.0);
        editWeight.setText(String.valueOf(originalWeight));

        originalActivity = intent.getDoubleExtra("activity", 1.1);
        setupSpinner();


        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }



    private void setupSpinner() {
        Spinner editActivity = findViewById(R.id.edit_activity);

        List<String> list = new ArrayList<>();
        list.add("Sedentary");
        list.add("Lightly active");
        list.add("Moderately active");
        list.add("Very active");
        list.add("Extremely active");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editActivity.setAdapter(dataAdapter);

        switch (String.valueOf(originalActivity)) {
            case "1.2": editActivity.setSelection(0); break;
            case "1.375": editActivity.setSelection(1); break;
            case "1.55": editActivity.setSelection(2); break;
            case "1.725": editActivity.setSelection(3); break;
            case "1.9": editActivity.setSelection(4); break;
            default: editActivity.setSelection(1);
        }
    }

    private void saveSettings() {

    }


}
