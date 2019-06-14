package com.example.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.cache.UserSettingsCache;
import com.example.macrologandroid.dtos.SettingsResponse;
import com.example.macrologandroid.dtos.UserSettingsResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.models.Gender;
import com.example.macrologandroid.services.UserService;
import com.example.macrologandroid.util.LocalDateParser;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private static final int ADJUST_INTAKE_INTAKE = 901;

    private String originalName;
    private LocalDate originalBirthday;
    private Gender originalGender;
    private int originalHeight;
    private double originalWeight;
    private double originalActivity;

    private TextInputEditText editName;
    private TextInputEditText editBirthday;
    private RadioGroup genderRadios;
    private TextInputEditText editHeight;
    private TextInputEditText editWeight;
    private Spinner editActivity;

    private boolean intake;

    private Button saveButton;

    private UserService userService = new UserService();
    private Disposable disposable;
    private TextInputLayout editBirthdayLayout;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADJUST_INTAKE_INTAKE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        editName = findViewById(R.id.edit_name);
        editName.addTextChangedListener(textChangedListener);

        editBirthdayLayout = findViewById(R.id.edit_birthday_layout);
        editBirthday = findViewById(R.id.edit_birthday);
        editBirthday.addTextChangedListener(textChangedListener);

        genderRadios = findViewById(R.id.radiogroup_gender);
        editHeight = findViewById(R.id.edit_height);
        editHeight.addTextChangedListener(textChangedListener);

        editWeight = findViewById(R.id.edit_weight);
        editWeight.addTextChangedListener(textChangedListener);

        Button backButton = findViewById(R.id.backbutton);
        saveButton = findViewById(R.id.savebutton);

        Intent intent = getIntent();
        intake = intent.getBooleanExtra("INTAKE", false);
        if (intake) {
            genderRadios.check(R.id.check_male);
            backButton = findViewById(R.id.backbutton);
            backButton.setVisibility(View.GONE);
            TextView intakeTitle = findViewById(R.id.intake_title);
            intakeTitle.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else {
            UserSettingsResponse settings = UserSettingsCache.getInstance().getCache();
            originalName = settings.getName();
            editName.setText(originalName);

            originalBirthday = settings.getBirthday();
            editBirthday.setText(originalBirthday.format(DateTimeFormatter.ISO_LOCAL_DATE));

            originalGender = settings.getGender();
            if (Gender.FEMALE.equals(originalGender)) {
                genderRadios.check(R.id.check_female);
            } else {
                genderRadios.check(R.id.check_male);
            }

            originalHeight = settings.getHeight();
            editHeight.setText(String.valueOf(originalHeight));

            originalWeight = settings.getWeight();
            editWeight.setText(String.valueOf(originalWeight));

            originalActivity = settings.getActivity();

            backButton.setOnClickListener(v -> finish());
        }
        setupSpinner();

        saveButton.setOnClickListener(v -> saveSettings());
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(EditPersonalDetailsActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
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
            case "1.2":
                editActivity.setSelection(0);
                break;
            case "1.375":
                editActivity.setSelection(1);
                break;
            case "1.55":
                editActivity.setSelection(2);
                break;
            case "1.725":
                editActivity.setSelection(3);
                break;
            case "1.9":
                editActivity.setSelection(4);
                break;
            default:
                editActivity.setSelection(0);
        }
    }

    private void saveSettings() {
        String newBirthday = Objects.requireNonNull(editBirthday.getText()).toString();
        LocalDate newDate = LocalDateParser.parse(newBirthday);
        if (newDate == null) {
            editBirthdayLayout.setError("Incorrect format");
        } else {
            List<Observable<ResponseBody>> obsList = new ArrayList<>();
            UserSettingsResponse userSettings = new UserSettingsResponse();

            String newName = Objects.requireNonNull(editName.getText()).toString();
            if (!newName.isEmpty() && !newName.equals(originalName)) {
                obsList.add(userService.putSetting(new SettingsResponse(1, "name", newName)));
                userSettings.setName(newName);
            }

            if (!newBirthday.isEmpty() && originalBirthday != newDate) {
                int age = Period.between(newDate, LocalDate.now()).getYears();
                obsList.add(userService.putSetting(new SettingsResponse(1, "birthday", newBirthday)));
                obsList.add(userService.putSetting(new SettingsResponse(1, "age", String.valueOf(age))));
                userSettings.setBirthday(newDate);
                userSettings.setAge(age);
            }

            RadioButton selected = findViewById(genderRadios.getCheckedRadioButtonId());
            String newGender = selected.getText().toString().toUpperCase();
            if (originalGender == null || !newGender.equals(originalGender.toString())) {
                obsList.add(userService.putSetting(new SettingsResponse(1, "gender", newGender)));
                userSettings.setGender(Gender.valueOf(newGender));
            }

            String newHeight = Objects.requireNonNull(editHeight.getText()).toString();
            if (!newHeight.isEmpty() && originalHeight != Integer.valueOf(newHeight)) {
                obsList.add(userService.putSetting(new SettingsResponse(1, "height", newHeight)));
                userSettings.setHeight(Integer.valueOf(newHeight));
            }

            String newWeight = Objects.requireNonNull(editWeight.getText()).toString();
            if (!newWeight.isEmpty() && !String.valueOf(originalWeight).equals(newWeight)) {
                obsList.add(userService.putSetting(new SettingsResponse(1, "weight", newWeight)));
                userSettings.setWeight(Double.valueOf(newWeight));
            }

            String item = (String) editActivity.getSelectedItem();
            String newActivity;
            switch (item) {
                case "Sedentary":
                    newActivity = "1.2";
                    break;
                case "Lightly active":
                    newActivity = "1.375";
                    break;
                case "Moderately active":
                    newActivity = "1.55";
                    break;
                case "Very active":
                    newActivity = "1.725";
                    break;
                case "Extremely active":
                    newActivity = "1.9";
                    break;
                default:
                    newActivity = "1.375";
            }
            if (!String.valueOf(originalActivity).equals(newActivity)) {
                obsList.add(userService.putSetting(new SettingsResponse(1, "activity", newActivity)));
                userSettings.setActivity(Double.valueOf(newActivity));
            }

            disposable = Observable.zip(obsList, i -> i)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        if (intake) {
                            Intent intent = new Intent(this, AdjustIntakeActivity.class);
                            intent.putExtra("userSettings", userSettings);
                            intent.putExtra("INTAKE", true);
                            startActivityForResult(intent, ADJUST_INTAKE_INTAKE);
                        } else {
                            UserSettingsCache.getInstance().clearCache();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    }, err -> Log.d(this.getLocalClassName(), err.getMessage()));
        }
    }

    private void checkEmptyTextViews() {
        boolean nameIsEmpty = Objects.requireNonNull(editName.getText()).toString().isEmpty();
        boolean birthdayIsEmpty = Objects.requireNonNull(editBirthday.getText()).toString().isEmpty();
        boolean heightIsEmpty = Objects.requireNonNull(editHeight.getText()).toString().isEmpty();
        boolean weightIsEmpty = Objects.requireNonNull(editWeight.getText()).toString().isEmpty();

        if (!nameIsEmpty && !birthdayIsEmpty && !heightIsEmpty && !weightIsEmpty) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEmptyTextViews();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
