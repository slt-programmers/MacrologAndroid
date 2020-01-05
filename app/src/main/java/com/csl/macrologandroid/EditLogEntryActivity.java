package com.csl.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.csl.macrologandroid.adapters.AutocompleteAdapter;
import com.csl.macrologandroid.dtos.DishResponse;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.IngredientResponse;
import com.csl.macrologandroid.dtos.LogEntryRequest;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.DishService;
import com.csl.macrologandroid.services.FoodService;
import com.csl.macrologandroid.services.LogEntryService;
import com.csl.macrologandroid.util.DateParser;
import com.csl.macrologandroid.util.KeyboardManager;
import com.csl.macrologandroid.util.SpinnerSetupUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class EditLogEntryActivity extends AppCompatActivity {

    private static final int ADD_FOOD_ID = 567;

    private Date selectedDate;
    private LogEntryService logEntryService;
    private FoodService foodService;
    private DishService dishService;

    private List<FoodResponse> allFood;
    private List<DishResponse> allDishes;

    private final List<String> autoCompleteList = new ArrayList<>();

    private Meal selectedMeal;

    private AutoCompleteTextView foodTextView;
    private FoodResponse selectedFood;

    private Spinner editPortionOrUnitSpinner;
    private TextInputEditText editGramsOrAmount;
    private TextInputLayout editGramsOrAmountLayout;

    private Button addButton;
    private Button addNewFoodButton;

    private LinearLayout logentryLayout;
    private List<LogEntryResponse> logEntries;
    private List<LogEntryResponse> copyEntries;
    private List<LogEntryResponse> newEntries;
    private Meal meal;
    private Button saveButton;
    private Disposable postDisposable;
    private Disposable foodDisposable;
    private Disposable deleteDisposable;
    private Disposable dishDisposable;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID && resultCode == Activity.RESULT_OK) {
            String foodName = (String) data.getSerializableExtra("FOOD_NAME");
            setNewlyAddedFood(foodName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (foodDisposable != null) {
            foodDisposable.dispose();
        }
        if (postDisposable != null) {
            postDisposable.dispose();
        }
        if (deleteDisposable != null) {
            deleteDisposable.dispose();
        }
        if (dishDisposable != null) {
            dishDisposable.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        selectedDate = (Date) getIntent().getSerializableExtra("DATE");

        logEntryService = new LogEntryService(getToken());
        foodService = new FoodService(getToken());
        dishService = new DishService(getToken());

        getFoodAndDishes();

        logEntries = new ArrayList<>();
        List entries = (List) getIntent().getSerializableExtra("LOGENTRIES");
        if (entries != null) {
            for (Object entry : entries) {
                if (entry instanceof LogEntryResponse) {
                    logEntries.add((LogEntryResponse) entry);
                }
            }

            if (logEntries.isEmpty()) {
                meal = (Meal) getIntent().getSerializableExtra("MEAL");
            } else {
                meal = logEntries.get(0).getMeal();
            }
            copyEntries = new ArrayList<>(logEntries);
        }

        setupMealSpinner();
        setupAutoCompleteTextView();

        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.GONE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmountLayout = findViewById(R.id.edit_grams_amount_layout);
        editGramsOrAmountLayout.setVisibility(View.GONE);

        logentryLayout = findViewById(R.id.logentry_layout);
        fillLogEntryLayout();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            saveLogEntries();
        });

        if (logEntries.isEmpty()) {
            saveButton.setVisibility(View.GONE);
        }

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            toggleFields(false);
            KeyboardManager.hideKeyboard(this);
            foodTextView.setText("");
            addLogEntry();
        });
        addButton.setEnabled(false);

        addNewFoodButton = findViewById(R.id.add_new_food_button);
        addNewFoodButton.setOnClickListener(v -> {
            Intent addFoodIntent = new Intent(this, AddFoodActivity.class);
            addFoodIntent.putExtra("FOOD_NAME", foodTextView.getText().toString());
            startActivityForResult(addFoodIntent, ADD_FOOD_ID);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(EditLogEntryActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    private void getFoodAndDishes() {
        allFood = null;
        allDishes = null;
        foodDisposable = foodService.getAllFood().subscribe(res -> {
            allFood = res;
            combineFoodAndDishesSearchList();
        }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));

        dishDisposable = dishService.getAllDishes().subscribe(res -> {
            allDishes = res;
            combineFoodAndDishesSearchList();
        }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void combineFoodAndDishesSearchList() {
        autoCompleteList.clear();
        if (allFood != null && allDishes != null) {
            fillAutoCompleteList();
            setupAutoCompleteTextView();
        }
    }

    private void fillAutoCompleteList() {
        autoCompleteList.clear();
        for (FoodResponse res : allFood) {
            autoCompleteList.add(res.getName());
        }
        for (DishResponse res : allDishes) {
            autoCompleteList.add(res.getName() + " (Dish)");
        }
        Collections.sort(autoCompleteList);

    }

    private void fillLogEntryLayout() {
        for (LogEntryResponse entry : logEntries) {
            addLogEntryToLayout(entry);
        }
    }

    private void toggleFields(boolean visible) {
        if (visible) {
            editPortionOrUnitSpinner.setVisibility(View.VISIBLE);
            editGramsOrAmountLayout.setVisibility(View.VISIBLE);
            editGramsOrAmount.requestFocus();
            addButton.setVisibility(View.VISIBLE);
            addButton.setEnabled(true);
        } else {
            editPortionOrUnitSpinner.setVisibility(View.GONE);
            editGramsOrAmountLayout.setVisibility(View.GONE);
            addButton.setEnabled(false);
        }
    }

    private void appendNewEntry(LogEntryResponse entry) {
        logEntries.add(entry);
        copyEntries.add(entry);

        addLogEntryToLayout(entry);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void addDishEntry(String dishName) {
        String dishFromInput = dishName.substring(0, dishName.length() - 7);
        DishResponse selectedDish = null;
        for (DishResponse dish : allDishes) {
            if (dish.getName().equalsIgnoreCase(dishFromInput)) {
                selectedDish = dish;
                break;
            }
        }

        if (selectedDish != null) {
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
            postDisposable = logEntryService.postLogEntry(entryList)
                    .subscribe(res -> {
                                newEntries = res;
                                for (LogEntryResponse newEntry : newEntries) {
                                    appendNewEntry(newEntry);
                                }
                            },
                            err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
        }
    }

    private void addLogEntry() {
        Long portionId = null;
        for (PortionResponse portion : selectedFood.getPortions()) {
            String portionDescription = (String) editPortionOrUnitSpinner.getSelectedItem();
            if (portionDescription.equals(portion.getDescription())) {
                portionId = portion.getId();
                break;
            }
        }

        double multiplier = 1.0;
        if (editGramsOrAmount.getText() != null) {
            multiplier = Double.valueOf(editGramsOrAmount.getText().toString());
            if (portionId == null) {
                multiplier = multiplier / 100;
            }
        }
        Long foodId = selectedFood.getId();
        LogEntryRequest entry = new LogEntryRequest(null, foodId, portionId,
                multiplier, DateParser.format(selectedDate),
                selectedMeal.toString());
        List<LogEntryRequest> entryList = new ArrayList<>();
        entryList.add(entry);
        postDisposable = logEntryService.postLogEntry(entryList)
                .subscribe(res -> {
                            newEntries = res;
                            appendNewEntry(newEntries.get(0));
                        },
                        err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void addLogEntryToLayout(LogEntryResponse entry) {
        @SuppressLint("InflateParams")
        ConstraintLayout logEntry = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);

        TextView foodNameTextView = logEntry.findViewById(R.id.food_name);
        foodNameTextView.setText(entry.getFood().getName());

        ImageView trashImageView = logEntry.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> toggleToRemoveEntry(entry));

        TextInputEditText foodAmount = logEntry.findViewById(R.id.food_amount);
        foodAmount.setId(R.id.food_amount);

        if (entry.getPortion() == null) {
            foodAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
            foodAmount.setText(String.valueOf(Math.round(entry.getMultiplier() * 100)));
        } else {
            foodAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            foodAmount.setText(String.valueOf(entry.getMultiplier()));
        }

        Spinner foodPortion = logEntry.findViewById(R.id.portion_spinner);
        setupPortionSpinner(foodPortion, entry, foodAmount);

        logentryLayout.addView(logEntry);
    }

    private void setNewlyAddedFood(String foodName) {
        addNewFoodButton.setVisibility(View.GONE);
        if (foodDisposable != null) {
            foodDisposable.dispose();
        }
        foodDisposable = foodService.getAllFood()
                .subscribe(res -> {
                    allFood = res;
                    fillAutoCompleteList();
                    setupAutoCompleteTextView();
                    foodTextView.setText(foodName);
                    setupPortionUnitSpinner(foodName);
                    toggleFields(true);
                }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private void toggleToRemoveEntry(LogEntryResponse entry) {
        int index = logEntries.indexOf(entry);

        ConstraintLayout logEntryLayout = (ConstraintLayout) logentryLayout.getChildAt(index);
        TextView foodName = (TextView) logEntryLayout.getChildAt(0);
        ImageView trashcan = (ImageView) logEntryLayout.getChildAt(1);
        View foodSpinner = logEntryLayout.getChildAt(2);
        View foodAmount = logEntryLayout.getChildAt(4);

        if (copyEntries.indexOf(entry) == -1) {
            // item was removed, so add it again
            if (index > copyEntries.size()) {
                copyEntries.add(entry);
            } else {
                copyEntries.add(index, entry);
            }
            foodName.setAlpha(1f);
            trashcan.setImageResource(R.drawable.trashcan);
            foodSpinner.setVisibility(View.VISIBLE);
            foodAmount.setVisibility(View.VISIBLE);
        } else {
            copyEntries.remove(entry);
            foodName.setAlpha(0.4f);
            trashcan.setImageResource(R.drawable.replay);
            foodSpinner.setVisibility(View.GONE);
            foodAmount.setVisibility(View.GONE);
        }
    }

    private void saveLogEntries() {
        List<LogEntryRequest> entries = new ArrayList<>();

        for (LogEntryResponse entry : logEntries) {
            if (copyEntries.indexOf(entry) == -1) {
                deleteEntry(entry);
            } else {
                LogEntryRequest request = makeLogEntryRequest(entry);
                entries.add(request);
            }
        }

        if (!entries.isEmpty()) {
            postDisposable = logEntryService.postLogEntry(entries)
                    .subscribe(res -> {
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    });
        } else {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private LogEntryRequest makeLogEntryRequest(LogEntryResponse entry) {
        int index = logEntries.indexOf(entry);
        ConstraintLayout logEntryLayout = (ConstraintLayout) logentryLayout.getChildAt(index);
        Spinner portionSpinner = (Spinner) logEntryLayout.getChildAt(2);
        String item = (String) portionSpinner.getSelectedItem();

        double multiplier = 1;
        TextInputEditText foodAmount = (TextInputEditText) ((TextInputLayout) logEntryLayout.getChildAt(4)).getEditText();
        if (foodAmount != null && foodAmount.getText() != null) {
            multiplier = Double.valueOf(foodAmount.getText().toString());
        }

        Long portionId = null;
        if (!item.equals("gram")) {
            for (PortionResponse portion : entry.getFood().getPortions()) {
                String trimmedItem = item.substring(0, item.indexOf('(')).trim();
                if (trimmedItem.equals(portion.getDescription())) {
                    portionId = portion.getId();
                    break;
                }
            }
        } else {
            multiplier = multiplier / 100;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return new LogEntryRequest(
                (long) entry.getId(),
                entry.getFood().getId(),
                portionId,
                multiplier,
                format.format(entry.getDay()),
                entry.getMeal().toString()
        );
    }

    private void deleteEntry(LogEntryResponse entry) {
        deleteDisposable = logEntryService.deleteLogEntry(entry.getId())
                .subscribe(res -> {
                        },
                        err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
    }

    private boolean isDish(String selectedName) {
        return selectedName != null && selectedName.endsWith(" (Dish)");
    }

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);
        ArrayAdapter<String> autocompleteAdapter = new AutocompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item, autoCompleteList);
        foodTextView.setAdapter(autocompleteAdapter);
        foodTextView.setThreshold(2);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
            String foodName = ((AppCompatCheckedTextView) view).getText().toString();
            if (isDish(foodName)) {
                foodTextView.setText("");
                addDishEntry(foodName);
            } else {
                setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
                toggleFields(true);
            }
            addNewFoodButton.setVisibility(View.INVISIBLE);
        });

        autocompleteAdapter.registerDataSetObserver(
                new DataSetObserver() {
                    @Override
                    public void onInvalidated() {
                        super.onInvalidated();
                        String text = foodTextView.getText().toString();
                        if (text.length() > 2 && !autoCompleteList.contains(text)) {
                            toggleFields(false);
                            addButton.setVisibility(View.INVISIBLE);
                            addNewFoodButton.setVisibility(View.VISIBLE);
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
                    if (isDish(selectedOption)) {
                        foodTextView.setText("");
                        addDishEntry(selectedOption);
                    } else {
                        foodTextView.setText(selectedOption);
                        foodTextView.dismissDropDown();
                        setupPortionUnitSpinner(selectedOption);
                        toggleFields(true);
                    }
                    addNewFoodButton.setVisibility(View.INVISIBLE);
                }
                return true;
            }
            return false;
        });
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

        selectedMeal = meal;
        String capitalizedString = meal.toString().substring(0, 1).toUpperCase() + meal.toString().substring(1).toLowerCase();
        mealtypeSpinner.setSelection(list.indexOf(capitalizedString));
        mealtypeSpinner.setEnabled(false);
        mealtypeSpinner.setClickable(false);
    }

    private void setupPortionSpinner(Spinner foodPortion, LogEntryResponse entry, TextInputEditText foodAmount) {
        List<String> list = new ArrayList<>();
        List<PortionResponse> allPortions = entry.getFood().getPortions();
        for (PortionResponse portion : allPortions) {
            String portionText = portion.getDescription() + " (" + portion.getGrams() + " gr)";
            list.add(portionText);
        }
        list.add("gram");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodPortion.setAdapter(dataAdapter);
        PortionResponse selectedPortion = entry.getPortion();
        if (selectedPortion != null) {
            foodPortion.setSelection(list.indexOf(selectedPortion.getDescription() + " (" +
                    selectedPortion.getGrams() + " gr)"));
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
                // Not needed
            }
        });
    }

    private void setupPortionUnitSpinner(String foodName) {
        SpinnerSetupUtil spinnerUtil = new SpinnerSetupUtil();
        selectedFood = spinnerUtil.getFoodFromList(foodName, allFood);
        if (selectedFood == null) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("PREF_PORTION", MODE_PRIVATE);
        spinnerUtil.setupPortionUnitSpinner(this, selectedFood, editPortionOrUnitSpinner, editGramsOrAmount, prefs);
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

}
