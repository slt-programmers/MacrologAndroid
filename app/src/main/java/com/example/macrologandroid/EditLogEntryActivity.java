package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.dtos.LogEntryRequest;
import com.example.macrologandroid.dtos.LogEntryResponse;
import com.example.macrologandroid.dtos.PortionResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.models.Meal;
import com.example.macrologandroid.services.FoodService;
import com.example.macrologandroid.services.LogEntryService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditLogEntryActivity extends AppCompatActivity {

    private static final int ADD_FOOD_ID = 567;

    private LocalDate selectedDate;
    private LogEntryService logEntryService;
    private FoodService foodService;
    private List<FoodResponse> allFood;
    private List<String> foodNames = new ArrayList<>();

    private Meal selectedMeal;

    private AutoCompleteTextView foodTextView;
    private ArrayAdapter<String> autocompleteAdapter;
    private FoodResponse selectedFood;

    private Spinner editPortionOrUnitSpinner;
    private EditText editGramsOrAmount;
    private TextInputLayout editGramsOrAmountLayout;

    private Button addButton;
    private Button addNewFoodButton;

    private LinearLayout logentryLayout;
    private List<LogEntryResponse> logEntries;
    private List<LogEntryResponse> copyEntries;
    private List<LogEntryResponse> newEntries;
    private Meal meal;
    private Button saveButton;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_FOOD_ID) {
            if (resultCode == Activity.RESULT_OK) {
                String foodName = (String) data.getSerializableExtra("FOOD_NAME");
                setNewlyAddedFood(foodName);
            }
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        selectedDate = (LocalDate) getIntent().getSerializableExtra("DATE");
        logEntryService = new LogEntryService();

        foodService = new FoodService();
        foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    allFood = res;
                    fillFoodNameList();
                    setupAutoCompleteTextView();
                }, err -> Log.d(this.getLocalClassName(), err.getMessage()));

        try {
            logEntries = (List<LogEntryResponse>) getIntent().getSerializableExtra("LOGENTRIES");
        } catch (Exception ex) {
            logEntries = new ArrayList<>();
        }
        if (logEntries.size() == 0) {
            meal = (Meal) getIntent().getSerializableExtra("MEAL");
        } else {
            meal = logEntries.get(0).getMeal();
        }
        copyEntries = new ArrayList<>(logEntries);

        setupMealSpinner();
        setupAutoCompleteTextView();
        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.GONE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmountLayout = findViewById(R.id.edit_grams_amount_layout);
        editGramsOrAmountLayout.setVisibility(View.GONE);

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            addButton.setEnabled(false);
            addLogEntry();
        });
        addButton.setEnabled(false);

        logentryLayout = findViewById(R.id.logentry_layout);
        fillLogEntrylayout();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            saveLogEntries();
        });


        if (logEntries.size() == 0) {
            saveButton.setVisibility(View.GONE);
        }

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            toggleFields(false);
            foodTextView.setText("");
            addLogEntry();
        });

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
        Session.getInstance().resetTimestamp();
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

    private void fillFoodNameList() {
        foodNames = new ArrayList<>();
        for (FoodResponse res : allFood) {
            foodNames.add(res.getName());
        }
    }

    private void fillLogEntrylayout() {
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

    private void appendNewEntry() {
        logEntries.addAll(newEntries);
        copyEntries.addAll(newEntries);
        addLogEntryToLayout(newEntries.get(0));
        saveButton.setVisibility(View.VISIBLE);
    }

    @SuppressLint("CheckResult")
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
        logEntryService.postLogEntry(entryList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                            newEntries = res;
                            appendNewEntry();
                        },
                        err -> {
                            Log.d(this.getLocalClassName(), err.getMessage());
                        });
    }

    private void addLogEntryToLayout(LogEntryResponse entry) {
        ConstraintLayout logEntry = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_log_entry, null);

        TextView foodNameTextView = logEntry.findViewById(R.id.food_name);
        foodNameTextView.setText(entry.getFood().getName());

        ImageView trashImageView = logEntry.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener((v) -> toggleToRemoveEntry(entry));

        EditText foodAmount = logEntry.findViewById(R.id.food_amount);
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

    @SuppressLint("CheckResult")
    private void setNewlyAddedFood(String foodName) {
        addNewFoodButton.setVisibility(View.GONE);
        foodService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    allFood = res;
                    fillFoodNameList();
                    setupAutoCompleteTextView();
                    foodTextView.setText(foodName);
                    setupPortionUnitSpinner(foodName);
                    toggleFields(true);
                }, err -> Log.d(this.getLocalClassName(), err.getMessage()));
    }

    @SuppressLint("CheckResult")
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

    @SuppressLint("CheckResult")
    private void saveLogEntries() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        List<LogEntryRequest> newEntries = new ArrayList<>();
        for (LogEntryResponse entry : logEntries) {
            if (copyEntries.indexOf(entry) == -1) {
                logEntryService.deleteLogEntry(entry.getId()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> Log.d(this.getLocalClassName(), res.string()),
                                err -> Log.d(this.getLocalClassName(), err.getMessage()));
            } else {
                int index = logEntries.indexOf(entry);
                ConstraintLayout logEntryLayout = (ConstraintLayout) logentryLayout.getChildAt(index);
                Spinner foodSpinner = (Spinner) logEntryLayout.getChildAt(2);
                String item = (String) foodSpinner.getSelectedItem();

                double multiplier = 1;
                EditText foodAmount = ((TextInputLayout) logEntryLayout.getChildAt(4)).getEditText();
                if (foodAmount!= null) {
                    multiplier = Double.valueOf(foodAmount.getText().toString());
                }

                Long portionId = null;
                if (!item.equals("gram")) {
                    for (PortionResponse portion : entry.getFood().getPortions()) {
                        if (item.contains(portion.getDescription())) {
                            portionId = (long) portion.getId();
                            break;
                        }
                    }
                } else {
                    multiplier = multiplier / 100;
                }

                LogEntryRequest request = new LogEntryRequest(
                        (long) entry.getId(),
                        entry.getFood().getId(),
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

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);
        autocompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, foodNames);
        foodTextView.setAdapter(autocompleteAdapter);
        foodTextView.setThreshold(1);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
            setupPortionUnitSpinner(((AppCompatCheckedTextView) view).getText().toString());
            toggleFields(true);
            addNewFoodButton.setVisibility(View.INVISIBLE);
        });

        autocompleteAdapter.registerDataSetObserver(
                new DataSetObserver() {
                    @Override
                    public void onInvalidated() {
                        super.onInvalidated();
                        String text = foodTextView.getText().toString();
                        if (text.length() > 2 && !foodNames.contains(text)) {
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
                    foodTextView.setText(selectedOption);
                    foodTextView.dismissDropDown();
                    setupPortionUnitSpinner(selectedOption);
                    toggleFields(true);
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

    private void setupPortionSpinner(Spinner foodPortion, LogEntryResponse entry, EditText foodAmount) {
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
            }
        });
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
