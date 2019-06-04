package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.dtos.LogEntryRequest;
import com.example.macrologandroid.dtos.PortionResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.models.Meal;
import com.example.macrologandroid.services.LogEntryService;
import com.example.macrologandroid.services.FoodService;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddLogEntryActivity extends AppCompatActivity {

    private static final int ADD_FOOD_ID = 567;

    private AutoCompleteTextView foodTextView;
    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private Button saveButton;
    private Button addButton;
    private FoodService foodService;
    private LogEntryService logService;
    private List<FoodResponse> allFood;
    private List<String> foodNames = new ArrayList<>();
    private ArrayAdapter<String> autocompleteAdapter;
    private FoodResponse selectedFood;
    private Meal selectedMeal;
    private LocalDate selectedDate;
    private Meal meal;
    private Disposable foodDisposable;
    private Disposable logDisposable;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID) {
            if (resultCode == Activity.RESULT_OK) {
                String foodName = (String) data.getSerializableExtra("FOOD_NAME");
                foodTextView.setText(foodName);
                setNewlyAddedFood(foodName);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_log_entry);

        Intent intent = getIntent();
        selectedDate = (LocalDate) intent.getSerializableExtra("DATE");
        meal = (Meal) intent.getSerializableExtra("MEAL");

        foodService = new FoodService();
        logService = new LogEntryService();
        foodDisposable = foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    allFood = res;
                    fillFoodNameList();
                    setupAutoCompleteTextView();
                }, err -> Log.d(this.getLocalClassName(), err.getMessage()));

        Button backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> finish());

        setupMealSpinner();
        setupAutoCompleteTextView();
        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.INVISIBLE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmount.setVisibility(View.INVISIBLE);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            addLogEntry();
        });
        saveButton.setVisibility(View.INVISIBLE);

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent addFoodIntent = new Intent(this, AddFoodActivity.class);
            addFoodIntent.putExtra("FOOD_NAME", foodTextView.getText().toString());
            startActivityForResult(addFoodIntent, ADD_FOOD_ID);
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
            Intent intent = new Intent(AddLogEntryActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (foodDisposable != null) {
            foodDisposable.dispose();
        }
        if (logDisposable != null) {
            logDisposable.dispose();
        }
    }

    private void setNewlyAddedFood(String foodName) {
        addButton.setVisibility(View.GONE);
        foodDisposable = foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    allFood = res;
                    fillFoodNameList();
                    setupAutoCompleteTextView();
                    foodTextView.setText(foodName);
                    setupPortionUnitSpinner(foodName);
                    toggleFields(true);
                }, err -> Log.d(this.getLocalClassName(), err.getMessage()));
    }

    private void addLogEntry() {
        Long portionId = null;
        for (PortionResponse portion : selectedFood.getPortions()) {
            String portionDescription = (String) editPortionOrUnitSpinner.getSelectedItem();
            if (portionDescription.equals(portion.getDescription())) {
                portionId = (long) portion.getId();
                break;
            }
        }

        double multiplier = Double.valueOf(editGramsOrAmount.getText().toString());
        if (portionId == null) {
            multiplier = multiplier / 100;
        }

        Long foodId = selectedFood.getId();
        LogEntryRequest entry = new LogEntryRequest(null, foodId, portionId,
                multiplier, selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                selectedMeal.toString());
        List<LogEntryRequest> entryList = new ArrayList<>();
        entryList.add(entry);
        logDisposable = logService.postLogEntry(entryList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("RELOAD", true);
                            resultIntent.putExtra("NEW_ENTRIES", (Serializable) res);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        },
                        err -> Log.d(this.getLocalClassName(), err.getMessage()));
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
            setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
            toggleFields(true);
        });

        autocompleteAdapter.registerDataSetObserver(
                new DataSetObserver() {
                    @Override
                    public void onInvalidated() {
                        super.onInvalidated();
                        if (foodTextView.getText().toString().length() > 2) {
                            toggleFields(false);
                        }
                    }
                }
        );

        foodTextView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT
                    && foodTextView.isPopupShowing()
                    && autocompleteAdapter.getCount() != 0) {
                String selectedOption = autocompleteAdapter.getItem(0);
                if (selectedOption != null) {
                    foodTextView.setText(selectedOption);
                    foodTextView.dismissDropDown();
                    setupPortionUnitSpinner(selectedOption);
                    toggleFields(true);
                }
                return true;
            }
            return false;
        });

    }

    private void toggleFields(boolean visible) {
        if (visible) {
            editPortionOrUnitSpinner.setVisibility(View.VISIBLE);
            editGramsOrAmount.requestFocus();
            saveButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
        } else {
            editPortionOrUnitSpinner.setVisibility(View.GONE);
            editGramsOrAmount.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupPortionUnitSpinner(String foodname) {
        selectedFood = allFood.stream().filter(f -> f.getName().trim().equals(foodname.trim())).findFirst().orElse(null);
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
                if (((AppCompatTextView) view).getText().toString().equals("gram")) {
                    editGramsOrAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editGramsOrAmount.setText(String.valueOf(100));
                    editGramsOrAmount.setSelection(3);
                } else {
                    editGramsOrAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editGramsOrAmount.setText(String.valueOf(1));
                    editGramsOrAmount.setSelection(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        editGramsOrAmount.setVisibility(View.VISIBLE);
    }

    private void setupMealSpinner() {
        Spinner mealtypeSpinner = findViewById(R.id.edit_meal_type);

        List<String> list = new ArrayList<>();
        list.add("Breakfast");
        list.add("Lunch");
        list.add("Dinner");
        list.add("Snacks");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealtypeSpinner.setAdapter(dataAdapter);

        if (meal == null) {
            setMealBasedOnTime(mealtypeSpinner);
            mealtypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (((AppCompatTextView) view).getText().toString()) {
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
                    foodTextView.requestFocus();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            selectedMeal = meal;
            String capitalizedString = meal.toString().substring(0, 1).toUpperCase() + meal.toString().substring(1).toLowerCase();
            mealtypeSpinner.setSelection(list.indexOf(capitalizedString));
            mealtypeSpinner.setEnabled(false);
            mealtypeSpinner.setClickable(false);
        }
    }

    private void setMealBasedOnTime(Spinner spinner) {
        LocalDateTime time = LocalDateTime.now();
        int hour = time.getHour();
        switch (hour) {
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
