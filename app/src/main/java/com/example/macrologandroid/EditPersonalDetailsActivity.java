package com.example.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.macrologandroid.DTO.UserSettingResponse;
import com.example.macrologandroid.Fragments.UserFragment;
import com.example.macrologandroid.Models.Gender;
import com.example.macrologandroid.Services.UserService;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private String originalName;
    private int originalAge;
    private Gender originalGender;
    private int originalHeight;
    private double originalWeight;
    private double originalActivity;

    private EditText editName;
    private EditText editAge;
    private RadioGroup genderRadios;
    private EditText editHeight;
    private EditText editWeight;
    private Spinner editActivity;

    private boolean oneOrMoreUpdated = false;

    private UserService userService = new UserService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        Intent intent = getIntent();
        editName = findViewById(R.id.edit_name);
        originalName = intent.getStringExtra("name");
        editName.setText(originalName);

        editAge = findViewById(R.id.edit_age);
        originalAge = intent.getIntExtra("age", 0);
        editAge.setText(String.valueOf(originalAge));

        genderRadios = findViewById(R.id.radiogroup_gender);
        originalGender = (Gender) intent.getSerializableExtra("gender");
        if (originalGender.equals(Gender.MALE)) {
            genderRadios.check(R.id.check_male);
        } else {
            genderRadios.check(R.id.check_female);
        }

        editHeight = findViewById(R.id.edit_height);
        originalHeight = intent.getIntExtra("height", 0);
        editHeight.setText(String.valueOf(originalHeight));

        editWeight = findViewById(R.id.edit_weight);
        originalWeight = intent.getDoubleExtra("weight", 0.0);
        editWeight.setText(String.valueOf(originalWeight));

        originalActivity = intent.getDoubleExtra("activity", 1.1);
        setupSpinner();

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(v -> {
            saveSettings();
            if (oneOrMoreUpdated) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("RELOAD", true);
                setResult(Activity.RESULT_OK, resultIntent);
            }
            finish();
        });
    }

    private void setupSpinner() {
        editActivity = findViewById(R.id.edit_activity);

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
        String newName = editName.getText().toString();
        if (!originalName.equals(newName)) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "name", newName)));
            oneOrMoreUpdated = true;
        }

        String newAge = editAge.getText().toString();
        if (originalAge != (Integer.valueOf(newAge))) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "age", newAge)));
            oneOrMoreUpdated = true;
        }

        RadioButton selected = findViewById(genderRadios.getCheckedRadioButtonId());
        String newGender = selected.getText().toString().toUpperCase();
        if (!newGender.equals(originalGender.toString())) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "gender", newGender)));
            oneOrMoreUpdated = true;
        }

        String newHeight = editHeight.getText().toString();
        if (originalHeight != Integer.valueOf(newHeight)) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "height", newHeight)));
            oneOrMoreUpdated = true;
        }

        String newWeight = editWeight.getText().toString();
        if (!String.valueOf(originalWeight).equals(String.valueOf(newWeight))) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "weight", newWeight)));
            oneOrMoreUpdated = true;
        }

        String item = (String) editActivity.getSelectedItem();
        String newActivity;
        switch (item) {
            case "Sedentary": newActivity = "1.2"; break;
            case "Lightly active": newActivity = "1.375"; break;
            case "Moderately active": newActivity = "1.55"; break;
            case "Very active": newActivity = "1.725"; break;
            case "Extremely active": newActivity = "1.9"; break;
            default: newActivity = "1.375";
        }
        if (!String.valueOf(originalActivity).equals(newActivity)) {
            subscribeToResult(userService.putSetting(new UserSettingResponse(1, "activity", newActivity)));
            oneOrMoreUpdated = true;
        }
    }

    private void subscribeToResult(Observable<ResponseEntity> serviceCall) {
        serviceCall.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> Log.d("MACROLOG", res.toString()),
                        err -> Log.d("MACROLOG", err.getMessage())
                        );

    }
}
