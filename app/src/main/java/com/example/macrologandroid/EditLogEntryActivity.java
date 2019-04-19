package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.DTO.LogEntryRequest;
import com.example.macrologandroid.DTO.LogEntryResponse;
import com.example.macrologandroid.DTO.PortionResponse;
import com.example.macrologandroid.Lifecycle.Session;
import com.example.macrologandroid.Models.Meal;
import com.example.macrologandroid.Services.LogEntryService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditLogEntryActivity extends AppCompatActivity {

    private static final int ADD_LOG_ENTRY_ID = 345;

    private LogEntryService logEntryService;
    private LinearLayout logentryLayout;
    private List<LogEntryResponse> logEntries;
    private List<LogEntryResponse> copyEntries;
    private List<LogEntryResponse> newEntries;
    private Meal meal;
    private Button saveButton;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (ADD_LOG_ENTRY_ID) : {
                if (resultCode == Activity.RESULT_OK) {
                    newEntries = (List<LogEntryResponse>) data.getSerializableExtra("NEW_ENTRIES");
                    appendNewEntry();
                }
                break;
            }
        }
    }

    private void appendNewEntry() {
        logEntries.addAll(newEntries);
        copyEntries.addAll(newEntries);
        addLogEntryToLayout(newEntries.get(0));
        saveButton.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        LocalDate selectedDate = (LocalDate) getIntent().getSerializableExtra("DATE");
        logEntryService = new LogEntryService();

        logentryLayout = findViewById(R.id.logentry_layout);
        logEntries = (List<LogEntryResponse>) getIntent().getSerializableExtra("LOGENTRIES");
        if (logEntries.size() == 0) {
            meal = (Meal) getIntent().getSerializableExtra("MEAL");
        } else {
            meal = logEntries.get(0).getMeal();
        }
        copyEntries = new ArrayList<>(logEntries);

        fillLogEntrylayout();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveLogEntries();
        });

        if (logEntries.size() == 0) {
            saveButton.setEnabled(false);
        }

        FloatingActionButton button = findViewById(R.id.floating_button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(EditLogEntryActivity.this, AddLogEntryActivity.class);
            intent.putExtra("DATE", selectedDate);
            intent.putExtra("MEAL", meal);
            startActivityForResult(intent, ADD_LOG_ENTRY_ID);
        });
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
            Intent intent = new Intent(EditLogEntryActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    private void fillLogEntrylayout() {
        for (LogEntryResponse entry : logEntries) {
            addLogEntryToLayout(entry);
        }
    }

    private void addLogEntryToLayout(LogEntryResponse entry) {
        ConstraintLayout logentry = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);

        TextView foodNameTextView = logentry.findViewById(R.id.food_name);
        foodNameTextView.setText(entry.getFood().getName());

        ImageView trashImageView = logentry.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener((v) -> {
            toggleToRemoveEntry(entry);
        });

        EditText foodAmount = logentry.findViewById(R.id.food_amount);
        foodAmount.setId(R.id.food_amount);

        if (entry.getPortion() == null) {
            foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
            foodAmount.setText(String.valueOf(Math.round(entry.getMultiplier() * 100)));
        } else {
            foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            foodAmount.setText(String.valueOf(entry.getMultiplier()));
        }

        Spinner foodPortion = logentry.findViewById(R.id.portion_spinner);
        setupSpinner(foodPortion, entry, foodAmount);

        logentryLayout.addView(logentry);
    }

    @SuppressLint("CheckResult")
    private void toggleToRemoveEntry(LogEntryResponse entry) {
        int index = logEntries.indexOf(entry);

        ConstraintLayout logEntryLayout = (ConstraintLayout) logentryLayout.getChildAt(index);
        TextView foodName = (TextView) logEntryLayout.getChildAt(0);
        ImageView trashcan = (ImageView) logEntryLayout.getChildAt(1);
        View foodSpinner = logEntryLayout.getChildAt(2);
        View foodAmount = logEntryLayout.getChildAt(3);

        if (copyEntries.indexOf(entry) == -1) {
            // item was removed, so add it again
            if (index > copyEntries.size()) {
                copyEntries.add(entry);
            } else {
                copyEntries.add(index, entry);
            }
            foodName.setAlpha(1f);
            trashcan.setImageResource(R.drawable.ic_trashcan);
            foodSpinner.setVisibility(View.VISIBLE);
            foodAmount.setVisibility(View.VISIBLE);
        } else {
            copyEntries.remove(entry);
            foodName.setAlpha(0.4f);
            trashcan.setImageResource(R.drawable.ic_replay);
            foodSpinner.setVisibility(View.GONE);
            foodAmount.setVisibility(View.GONE);
        }
    }

    @SuppressLint("CheckResult")
    private void saveLogEntries() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        List<LogEntryRequest> newEntries = new ArrayList<>();
        for (LogEntryResponse entry : logEntries) {
            if (copyEntries.indexOf(entry) == -1) {
                logEntryService.deleteLogEntry(entry.getId()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {

                                },
                                err -> {

                                });
            } else {
                int index = logEntries.indexOf(entry);
                ConstraintLayout logEntryLayout = (ConstraintLayout) logentryLayout.getChildAt(index);
                Spinner foodSpinner = (Spinner) logEntryLayout.getChildAt(2);
                String item = (String) foodSpinner.getSelectedItem();

                EditText foodAmount = ((TextInputLayout) logEntryLayout.getChildAt(3)).getEditText();
                double multiplier = Double.valueOf(foodAmount.getText().toString());

                Long portionId = null;
                if (!item.equals("gram")) {
                    for (PortionResponse portion : entry.getFood().getPortions()) {
                        if (portion.getDescription().equals(item)) {
                            portionId = (long) portion.getId();
                            break;
                        }
                    }
                } else {
                    multiplier = multiplier / 100;
                }

                LogEntryRequest request = new LogEntryRequest(
                        (long) entry.getId(),
                        (long) entry.getFood().getId(),
                        portionId,
                        multiplier,
                        format.format(entry.getDay()),
                        entry.getMeal().toString()
                );
                newEntries.add(request);
            }
        }

        logEntryService.postLogEntry(newEntries).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("RELOAD", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                });
    }

    private void setupSpinner(Spinner foodPortion, LogEntryResponse entry, EditText foodAmount) {
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

        foodPortion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((AppCompatTextView) view).getText().toString().equals("gram")) {
                    foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    foodAmount.setText(String.valueOf(Math.round(entry.getMultiplier() * 100)));
                } else {
                    foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    foodAmount.setText(String.valueOf(entry.getMultiplier()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
