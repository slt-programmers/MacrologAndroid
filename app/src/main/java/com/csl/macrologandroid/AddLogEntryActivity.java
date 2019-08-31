package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;

import com.csl.macrologandroid.adapters.AutocompleteAdapter;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.IngredientResponse;
import com.csl.macrologandroid.dtos.LogEntryRequest;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.services.FoodService;
import com.csl.macrologandroid.services.LogEntryService;
import com.csl.macrologandroid.util.DateParser;
import com.csl.macrologandroid.util.SpinnerSetupUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class AddLogEntryActivity extends AppCompatActivity {

    private static final int ADD_FOOD_ID = 567;

    private AutoCompleteTextView foodTextView;
    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private Button saveButton;
    private Button addButton;
    private FoodService foodService;
    private DishService dishService;
    private LogEntryService logService;
    private List<FoodResponse> allFood;
    private List<DishResponse> allDishes;
    private List<String> autoCompleteList = new ArrayList<>();
    private FoodResponse selectedFood;
    private Meal selectedMeal;
    private Date selectedDate;
    private Disposable foodDisposable;
    private Disposable logDisposable;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID && resultCode == Activity.RESULT_OK) {
            String foodName = (String) data.getSerializableExtra("FOOD_NAME");
            foodTextView.setText(foodName);
            setNewlyAddedFood(foodName);
        }
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
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    private void setupFoodAndDishes(){
        allFood = null;
        allDishes = null;
        foodService.getAllFood().subscribe(res -> {
            allFood= res;
            checkFoodAndDishesResponse();
        },  err -> Log.e(this.getLocalClassName(), err.getMessage()));

        dishService.getAllDishes().subscribe(res -> {
            allDishes= res;
            checkFoodAndDishesResponse();
        },  err -> Log.e(this.getLocalClassName(), err.getMessage()));
    }

    private void checkFoodAndDishesResponse(){
        autoCompleteList = new ArrayList<>();
        if (allFood != null && allDishes!= null){
            fillAutoCompleteList();
            setupAutoCompleteTextView();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_log_entry);

        Intent intent = getIntent();
        selectedDate = (Date) intent.getSerializableExtra("DATE");

        foodService = new FoodService(getToken());
        dishService = new DishService(getToken());
        logService = new LogEntryService(getToken());

        setupFoodAndDishes();

        Button backbutton = findViewById(R.id.back_button);
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
        foodDisposable = foodService.getAllFood()
                .subscribe(res -> {
                    allFood = res;
                    fillAutoCompleteList();
                    setupAutoCompleteTextView();
//                    foodTextView.setText(foodName);
                    setupPortionUnitSpinner(foodName);
                    toggleFields(true);
                }, err -> Log.e(this.getLocalClassName(), err.getMessage()));
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

        double multiplier = Double.parseDouble(Objects.requireNonNull(editGramsOrAmount.getText()).toString());
        if (portionId == null) {
            multiplier = multiplier / 100;
        }

        Long foodId = selectedFood.getId();
        LogEntryRequest entry = new LogEntryRequest(null, foodId, portionId,
                multiplier, DateParser.format(selectedDate),
                selectedMeal.toString());
        List<LogEntryRequest> entryList = new ArrayList<>();
        entryList.add(entry);
        logDisposable = logService.postLogEntry(entryList)
                .subscribe(res -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("RELOAD", true);
                            resultIntent.putExtra("NEW_ENTRIES", (Serializable) res);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        },
                        err -> Log.e(this.getLocalClassName(), err.getMessage()));
    }

    private void addDishEntry(String dishName) {
        // dishName = dish.name + " (Dish)"
        String dishFromInput = dishName.substring(0,dishName.length() - 7);
        DishResponse selectedDish = null;
        for (DishResponse dish : allDishes) {
            if (dish.getName().equalsIgnoreCase(dishFromInput)){
              selectedDish = dish;
              break;
            }
        }

        List<LogEntryRequest> entryList = new ArrayList<>();
        for (IngredientResponse ingredient : selectedDish.getIngredients()) {
            Long portionId = ingredient.getPortionId();
            double multiplier = ingredient.getMultiplier();

            Long foodId = ingredient.getFood().getId();
            LogEntryRequest entry = new LogEntryRequest(null, foodId, portionId,
                    multiplier, DateParser.format(selectedDate),
                    selectedMeal.toString());
            entryList.add(entry);
        }
        logDisposable = logService.postLogEntry(entryList)
                .subscribe(res -> {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("RELOAD", true);
                            resultIntent.putExtra("NEW_ENTRIES", (Serializable) res);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        },
                        err -> Log.e(this.getLocalClassName(), err.getMessage()));

    }

    private void fillAutoCompleteList() {
        autoCompleteList = new ArrayList<>();
        for (FoodResponse res : allFood) {
            autoCompleteList.add(res.getName());
        }
        for (DishResponse res : allDishes) {
            autoCompleteList.add(res.getName() +" (Dish)");
        }
    }
    private boolean isDish(String selectedName) {
        return selectedName!= null && selectedName.endsWith(" (Dish)");
    }

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);
        ArrayAdapter<String> autocompleteAdapter = new AutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item, autoCompleteList);
        foodTextView.setAdapter(autocompleteAdapter);
        foodTextView.setThreshold(1);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
            String foodName = ((AppCompatCheckedTextView) view).getText().toString();
            if (isDish(foodName)) {
                addDishEntry(foodName);
            } else {
                setupPortionUnitSpinner(foodName);
                toggleFields(true);
            }
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
                    if (isDish(selectedOption)) {
                        addDishEntry(selectedOption);
                    } else {
                        setupPortionUnitSpinner(selectedOption);
                        toggleFields(true);
                    }
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

    private void setupPortionUnitSpinner(String foodName) {
        SpinnerSetupUtil spinnerUtil = new SpinnerSetupUtil();
        selectedFood = spinnerUtil.getFoodFromList(foodName, allFood);
        if (selectedFood == null) {
            return;
        }
        spinnerUtil.setupPortionUnitSpinner(this, selectedFood, editPortionOrUnitSpinner, editGramsOrAmount);
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
                // Not needed
            }
        });
    }

    private void setMealBasedOnTime(Spinner spinner) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
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

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
