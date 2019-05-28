package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.dtos.PortionResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.services.FoodService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddFoodActivity extends AppCompatActivity {

    private EditText editFoodName;
    private EditText editProtein;
    private EditText editFat;
    private EditText editCarbs;
    private LinearLayout portionsLayout;
    private Button saveButton;

    private FoodResponse foodResponse;

    private Disposable disposable;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            isSaveButtonEnabled();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);


        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        foodResponse = (FoodResponse) intent.getSerializableExtra("FOOD_RESPONSE");
        String foodName = intent.getStringExtra("FOOD_NAME");

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
            editFoodName.requestFocus();
            saveButton.setEnabled(false);
        }
    }

    private void isSaveButtonEnabled() {
        boolean nameCheck = editFoodName.getText() != null && editFoodName.getText().toString().length() != 0;
        boolean proteinCheck = editProtein.getText() != null && editProtein.getText().toString().length() != 0;
        boolean fatCheck = editFat.getText() != null && editFat.getText().toString().length() != 0;
        boolean carbCheck = editCarbs.getText() != null && editCarbs.getText().toString().length() != 0;

        boolean portionsCheck = true;
        for (int i = 0; i < portionsLayout.getChildCount(); i++) {
            ConstraintLayout inner = (ConstraintLayout) portionsLayout.getChildAt(i);
            EditText portionDescription = inner.findViewById(R.id.portion_description);
            EditText portionGrams = inner.findViewById(R.id.portion_grams);
            if (portionDescription.getText() == null || portionDescription.getText().toString().length() == 0) {
                portionsCheck = false;
            }
            if (portionGrams.getText() == null || portionGrams.getText().toString().length() == 0) {
                portionsCheck = false;
            }
        }

        if (nameCheck && proteinCheck && fatCheck && carbCheck && portionsCheck) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(AddFoodActivity.this, SplashscreenActivity.class);
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

    private void addPortion(LinearLayout container, PortionResponse portion) {
        ConstraintLayout newPortionLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_add_portion, container, false);
        EditText portionDescription = newPortionLayout.findViewById(R.id.portion_description);
        portionDescription.addTextChangedListener(textWatcher);

        EditText portionGrams = newPortionLayout.findViewById(R.id.portion_grams);
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

    @SuppressLint("CheckResult")
    private void saveFood() {
        String name = editFoodName.getText().toString();
        double protein = Double.valueOf(editProtein.getText().toString());
        double fat = Double.valueOf(editFat.getText().toString());
        double carbs = Double.valueOf(editCarbs.getText().toString());

        List<PortionResponse> portions = new ArrayList<>();
        int childCount = portionsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ConstraintLayout inner = (ConstraintLayout) portionsLayout.getChildAt(i);
            EditText portionDescription = inner.findViewById(R.id.portion_description);
            EditText portionGrams = inner.findViewById(R.id.portion_grams);
            PortionResponse portion = new PortionResponse(0,
                    Double.valueOf(portionGrams.getText().toString()),
                    portionDescription.getText().toString(), null);
            portions.add(portion);
        }
        FoodResponse newFood = new FoodResponse(null, name, protein, fat, carbs, portions);
        if (foodResponse != null) {
            newFood.setId(foodResponse.getId());
        }
        FoodService foodService = new FoodService();
        disposable = foodService.postFood(newFood).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("FOOD_NAME", editFoodName.getText().toString());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }, err -> Log.d(this.getLocalClassName(), err.getMessage()));

    }
}
