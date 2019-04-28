package com.example.macrologandroid;

import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AddFoodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        Intent intent = getIntent();
        String foodName = intent.getStringExtra("FOOD_NAME");

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        EditText editFoodName = findViewById(R.id.food_name);
        editFoodName.setText(foodName);
        editFoodName.requestFocus();

        LinearLayout portionsLayout = findViewById(R.id.portions_layout);
        ImageView plus = findViewById(R.id.plus);
        plus.setOnClickListener(v -> {
            addPortion(portionsLayout);
        });
    }

    private void addPortion(LinearLayout container) {
        ConstraintLayout newPortionLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_add_portion, container, false);
        container.addView(newPortionLayout);
    }
}
