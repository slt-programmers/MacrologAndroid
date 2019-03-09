package com.example.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.macrologandroid.Models.Gender;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        Intent intent = getIntent();
        EditText editName = findViewById(R.id.edit_name);
        editName.setText(intent.getStringExtra("name"));

        EditText editAge = findViewById(R.id.edit_age);
        editAge.setText(String.valueOf(intent.getIntExtra("age", 0)));

        RadioGroup genderRadios = findViewById(R.id.radiogroup_gender);

        Gender gender = (Gender) intent.getSerializableExtra("gender");

        if (gender.equals(Gender.MALE)) {
            genderRadios.check(R.id.check_male);
        } else {
            genderRadios.check(R.id.check_female);
        }

        EditText editHeight = findViewById(R.id.edit_height);
        editHeight.setText(String.valueOf(intent.getIntExtra("height", 0)));

        EditText editWeight = findViewById(R.id.edit_weight);
        editWeight.setText(String.valueOf(intent.getDoubleExtra("weight", 0.0)));

        EditText editActivity = findViewById(R.id.edit_activity);
        editActivity.setText(String.valueOf(intent.getDoubleExtra("activity", 1.1)));


        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }

    private void saveSettings() {

    }


}
