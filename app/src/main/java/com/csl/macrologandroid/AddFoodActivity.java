package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.Portion;
import com.csl.macrologandroid.services.FoodRepository;
import com.csl.macrologandroid.viewmodels.FoodViewModel;
import com.csl.macrologandroid.viewmodels.ViewModelFactory;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddFoodActivity extends AppCompatActivity {

    private TextInputEditText editFoodName;
    private TextInputEditText editProtein;
    private TextInputEditText editFat;
    private TextInputEditText editCarbs;
    private LinearLayout portionsLayout;
    private Button saveButton;
    private TextInputLayout editFoodNameLayout;

    private FoodResponse foodResponse;
    private final List<String> allFoodNames = new ArrayList<>();
    private FoodViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(getToken())).get(FoodViewModel.class);

        setContentView(R.layout.activity_add_food);

        List<Food> allFood = FoodCache.getInstance().getCache();
        for (Food food : allFood) {
            allFoodNames.add(food.getName());
        }

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        foodResponse = (FoodResponse) intent.getSerializableExtra("FOOD_RESPONSE");
        String foodName = intent.getStringExtra("FOOD_NAME");

        editFoodNameLayout = findViewById(R.id.food_name_layout);
        editFoodName = findViewById(R.id.food_name);
        editProtein = findViewById(R.id.edit_protein);
        editProtein.addTextChangedListener(textWatcher);
        editFat = findViewById(R.id.edit_fat);
        editFat.addTextChangedListener(textWatcher);
        editCarbs = findViewById(R.id.edit_carbs);
        editCarbs.addTextChangedListener(textWatcher);
        portionsLayout = findViewById(R.id.portions_layout);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveFood());

        ImageView plus = findViewById(R.id.plus);
        plus.setOnClickListener(v -> {
            addPortion(portionsLayout, null);
            saveButton.setEnabled(false);
        });

        if (foodResponse != null) {
            editFoodName.setText(foodResponse.getName());
            editProtein.setText(String.valueOf(foodResponse.getProtein()));
            editFat.setText(String.valueOf(foodResponse.getFat()));
            editCarbs.setText(String.valueOf(foodResponse.getCarbs()));
            for (PortionResponse portion : foodResponse.getPortions()) {
                addPortion(portionsLayout, portion);
            }
            saveButton.setEnabled(false);
        } else {
            editFoodName.setText(foodName);
            editFoodName.addTextChangedListener(foodNameWatcher);
            editFoodName.requestFocus();
            saveButton.setEnabled(false);
        }
    }

    private void isSaveButtonEnabled() {
        boolean nameCheck = editFoodName.getText() != null && editFoodName.getText().toString().length() != 0 && !matchingFoodName(editFoodName.getText().toString());
        boolean proteinCheck = editProtein.getText() != null && editProtein.getText().toString().length() != 0;
        boolean fatCheck = editFat.getText() != null && editFat.getText().toString().length() != 0;
        boolean carbCheck = editCarbs.getText() != null && editCarbs.getText().toString().length() != 0;

        boolean portionsCheck = true;
        for (int i = 0; i < portionsLayout.getChildCount(); i++) {
            ConstraintLayout inner = (ConstraintLayout) portionsLayout.getChildAt(i);
            TextInputEditText portionDescription = inner.findViewById(R.id.portion_description);
            TextInputEditText portionGrams = inner.findViewById(R.id.portion_grams);
            if (portionDescription.getText() == null || portionDescription.getText().toString().length() == 0) {
                portionsCheck = false;
            }
            if (portionGrams.getText() == null || portionGrams.getText().toString().length() == 0) {
                portionsCheck = false;
            }
        }

        saveButton.setEnabled(nameCheck && proteinCheck && fatCheck && carbCheck && portionsCheck);

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
            Intent intent = new Intent(AddFoodActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    private void addPortion(LinearLayout container, PortionResponse portion) {
        ConstraintLayout newPortionLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_add_portion, container, false);
        TextInputEditText portionDescription = newPortionLayout.findViewById(R.id.portion_description);
        portionDescription.addTextChangedListener(textWatcher);

        TextInputEditText portionGrams = newPortionLayout.findViewById(R.id.portion_grams);
        portionGrams.addTextChangedListener(textWatcher);

        ImageView trashcan = newPortionLayout.findViewById(R.id.trash_icon);

        if (portion != null) {
            portionDescription.setText(portion.getDescription());
            portionGrams.setText(String.valueOf(portion.getGrams()));
            trashcan.setVisibility(View.INVISIBLE);
        } else {
            trashcan.setOnClickListener(v -> removePortion(newPortionLayout));
        }

        container.addView(newPortionLayout);
    }

    private void removePortion(ConstraintLayout portionLayout) {
        portionsLayout.removeView(portionLayout);
        isSaveButtonEnabled();
    }

    private void saveFood() {
        String name = Objects.requireNonNull(editFoodName.getText()).toString();
        double protein = Double.parseDouble(Objects.requireNonNull(editProtein.getText()).toString());
        double fat = Double.parseDouble(Objects.requireNonNull(editFat.getText()).toString());
        double carbs = Double.parseDouble(Objects.requireNonNull(editCarbs.getText()).toString());

        List<Portion> portions = new ArrayList<>();
        int childCount = portionsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ConstraintLayout inner = (ConstraintLayout) portionsLayout.getChildAt(i);
            TextInputEditText portionDescription = inner.findViewById(R.id.portion_description);
            TextInputEditText portionGrams = inner.findViewById(R.id.portion_grams);
            String description = Objects.requireNonNull(portionDescription.getText()).toString();
            Portion portion = new Portion(findIdForPortion(i),
                    Double.valueOf(Objects.requireNonNull(portionGrams.getText()).toString()), description);
            portions.add(portion);
        }
        Food newFood = new Food(null, name, protein, fat, carbs, portions);
        if (foodResponse != null) {
            newFood.setId(foodResponse.getId());
        }
        viewModel.postFood(newFood);
        FoodRepository foodRepository = new FoodRepository(getToken());
        disposable = foodRepository.postFoodObservable(newFood)
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("FOOD_NAME", editFoodName.getText().toString());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }, err -> Log.e(this.getLocalClassName(), err.getMessage()));

    }

    private Integer findIdForPortion(int index) {
        if (foodResponse != null) {
            List<PortionResponse> portions = foodResponse.getPortions();
            if (portions != null && !portions.isEmpty()) {
                try {
                    PortionResponse portion = portions.get(index);
                    return portion.getId();
                } catch (Exception ex) {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not needed
        }

        @Override
        public void afterTextChanged(Editable s) {
            isSaveButtonEnabled();
        }
    };

    private final TextWatcher foodNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (matchingFoodName(s.toString())) {
                editFoodNameLayout.setErrorEnabled(true);
                editFoodNameLayout.setError("Food already in database");
            } else {
                editFoodNameLayout.setErrorEnabled(false);
                editFoodNameLayout.setError("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            isSaveButtonEnabled();
        }
    };

    private boolean matchingFoodName(String foodName) {
        return allFoodNames.contains(foodName);
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
