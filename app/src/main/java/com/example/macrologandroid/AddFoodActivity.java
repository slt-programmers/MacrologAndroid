package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.macrologandroid.DTO.FoodResponse;
import com.example.macrologandroid.DTO.PortionResponse;
import com.example.macrologandroid.Lifecycle.Session;
import com.example.macrologandroid.Services.FoodService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddFoodActivity extends AppCompatActivity {

    private EditText editFoodName;
    private EditText editProtein;
    private EditText editFat;
    private EditText editCarbs;
    private LinearLayout portionsLayout;
    private FoodService foodService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        Intent intent = getIntent();
        String foodName = intent.getStringExtra("FOOD_NAME");

        foodService = new FoodService();

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        editFoodName = findViewById(R.id.food_name);
        editFoodName.setText(foodName);
        editFoodName.requestFocus();

        editProtein = findViewById(R.id.edit_protein);
        editFat = findViewById(R.id.edit_fat);
        editCarbs = findViewById(R.id.edit_carbs);

        portionsLayout = findViewById(R.id.portions_layout);
        ImageView plus = findViewById(R.id.plus);
        plus.setOnClickListener(v -> {
            addPortion(portionsLayout);
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveFood();
        });
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

    private void addPortion(LinearLayout container) {
        ConstraintLayout newPortionLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_add_portion, container, false);
        container.addView(newPortionLayout);
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
        foodService.postFood(newFood).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("FOOD_NAME", editFoodName.getText().toString());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }, err -> {
                    System.out.println(err.getMessage());
                });

    }
}
