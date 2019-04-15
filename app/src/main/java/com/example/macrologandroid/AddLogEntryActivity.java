package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.DTO.FoodResponse;
import com.example.macrologandroid.DTO.LogEntryRequest;
import com.example.macrologandroid.DTO.PortionResponse;
import com.example.macrologandroid.Lifecycle.Session;
import com.example.macrologandroid.Models.Meal;
import com.example.macrologandroid.Services.DiaryLogService;
import com.example.macrologandroid.Services.FoodService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddLogEntryActivity extends AppCompatActivity {

    private Spinner mealtypeSpinner;
    private AutoCompleteTextView foodTextView;
    private Spinner editPortionOrUnitSpinner;
    private EditText editGramsOrAmount;
    private Button saveButton;
    private FoodService foodService;
    private DiaryLogService logService;
    private List<FoodResponse> allFood;
    private List<String> foodNames = new ArrayList<>();
    private ArrayAdapter<String> autocompleteAdapter;
    private FoodResponse selectedFood;
    private Meal selectedMeal;
    private LocalDate selectedDate;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_log_entry);

        Intent intent = getIntent();
        selectedDate = (LocalDate) intent.getSerializableExtra("date");

        foodService = new FoodService();
        logService = new DiaryLogService();
        foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    allFood = res;
                    fillFoodNameList();
                    setupAutoCompleteTextView();
                }, err -> {
                    Log.d("FoodService",  err.getMessage());
                });

        Button backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> {
            finish();
        });

        setupMealSpinner();
        setupAutoCompleteTextView();
        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.GONE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmount.setVisibility(View.GONE);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            addLogEntry();
        });
        saveButton.setVisibility(View.GONE);
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
            startActivity(new Intent(AddLogEntryActivity.this, SplashscreenActivity.class));
        }
    }

    @SuppressLint("CheckResult")
    private void addLogEntry() {
        Long portionId = null;
        for (PortionResponse portion: selectedFood.getPortions()) {
            String portionDescription = (String) editPortionOrUnitSpinner.getSelectedItem();
            if (portionDescription.equals(portion.getDescription())) {
                portionId = (long) portion.getId();
                break;
            }
        }

        double multiplier = Double.valueOf(editGramsOrAmount.getText().toString());
        if (portionId == null) {
            multiplier = multiplier/100;
        }

        Long foodId = (long) selectedFood.getId();
        LogEntryRequest entry = new LogEntryRequest(null, foodId, portionId,
                multiplier, selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                selectedMeal.toString());
        List<LogEntryRequest> entryList = new ArrayList<>();
        entryList.add(entry);
        logService.postLogEntry(entryList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("RELOAD", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                        },
                        err -> {
                    Log.d("LogService", err.getMessage());
                        });
    }

    private void fillFoodNameList() {
        foodNames = new ArrayList<>();
        for (FoodResponse res : allFood) {
            foodNames.add(res.getName());
        }
    }

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);
        autocompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, foodNames);
        foodTextView.setAdapter(autocompleteAdapter);
        foodTextView.setThreshold(1);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
                editPortionOrUnitSpinner.setVisibility(View.VISIBLE);
                setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
                saveButton.setVisibility(View.VISIBLE);
        });

//        foodTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                    if (autocompleteAdapter.getCount() != 0) {
//                        foodTextView.setSelection(0);
//                        foodTextView.performCompletion();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });

    }

    private void setupPortionUnitSpinner(String foodname) {
        selectedFood = allFood.stream().filter(f -> f.getName().equals(foodname)).findFirst().orElse(null);
        List<String> list = new ArrayList<>();

        for (PortionResponse portion : selectedFood.getPortions()) {
            String desc = portion.getDescription();
            if (desc != null && !desc.isEmpty()) {
                list.add(desc);
            }
        }
        list.add("gram");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editPortionOrUnitSpinner.setAdapter(dataAdapter);
        editPortionOrUnitSpinner.setSelection(0);
        editPortionOrUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((AppCompatTextView)view).getText().toString().equals("gram")) {
                    editGramsOrAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editGramsOrAmount.setText("100");
                } else {
                    editGramsOrAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editGramsOrAmount.setText("1");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        editGramsOrAmount.setVisibility(View.VISIBLE);
    }

    private void setupMealSpinner() {
        mealtypeSpinner = findViewById(R.id.edit_meal_type);

        List<String> list = new ArrayList<>();
        list.add("Breakfast");
        list.add("Lunch");
        list.add("Dinner");
        list.add("Snacks");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealtypeSpinner.setAdapter(dataAdapter);
        setMealBasedOnTime(mealtypeSpinner);
        mealtypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(((AppCompatTextView)view).getText().toString()) {
                    case "Breakfast":
                        selectedMeal = Meal.BREAKFAST;
                        break;
                    case "Lunch":
                        selectedMeal = Meal.LUNCH;
                        break;
                    case "Dinner":
                        selectedMeal = Meal.DINNER;
                        break;
                    case "Snacks":
                        selectedMeal = Meal.SNACKS;
                        break;
                    default:
                        selectedMeal = Meal.BREAKFAST;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setMealBasedOnTime(Spinner spinner) {
        LocalDateTime time = LocalDateTime.now();
        int hour = time.getHour();
        switch(hour) {
            case 7:
            case 8:
            case 9:
                spinner.setSelection(0);
                break;
            case 12:
            case 13:
                spinner.setSelection(1);
                break;
            case 17:
            case 18:
            case 19:
                spinner.setSelection(2);
                break;
            default:
                spinner.setSelection(3);
        }
    }
}
