package com.example.macrologandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.macrologandroid.DTO.FoodResponse;
import com.example.macrologandroid.Models.MeasurementUnit;

import java.util.ArrayList;
import java.util.List;

public class AddLogEntryActivity extends AppCompatActivity {

    private Spinner mealtypeSpinner;
    private AutoCompleteTextView foodTextView;
    private Spinner editPortionOrUnitSpinner;
    private EditText editGramsOrAmount;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_log_entry);

        Button backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> {
            finish();
        });

        setupSpinner();
        setupAutoCompleteTextView();
        editPortionOrUnitSpinner = findViewById(R.id.edit_portion_unit);
        editPortionOrUnitSpinner.setVisibility(View.GONE);
        editGramsOrAmount = findViewById(R.id.edit_grams_amount);
        editGramsOrAmount.setVisibility(View.GONE);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            //TODO: save entry
            finish();
        });
        saveButton.setVisibility(View.GONE);
    }

    private void setupAutoCompleteTextView() {
        foodTextView = findViewById(R.id.edit_food_textview);

//        List<FoodResponse> foodList = new ArrayList<>();
//        foodList.add(new FoodResponse(1, "Apple", MeasurementUnit.UNIT, "stuk",
//                165, 1.2, 0.2, 45, null));
//        foodList.add(new FoodResponse(2, "Pasta", MeasurementUnit.GRAMS, null,
//                0, 11.2, 2.2, 65, null));
        List<String> foodList = new ArrayList<>();
        foodList.add("Apple");
        foodList.add("Apple Pie");
        foodList.add("Pasta");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, foodList);
        foodTextView.setAdapter(adapter);
        foodTextView.setThreshold(1);
        foodTextView.setOnItemClickListener((parent, view, position, id) -> {
                editPortionOrUnitSpinner.setVisibility(View.VISIBLE);
                setupPortionUnitSpinner();
                editGramsOrAmount.setVisibility(View.VISIBLE);
                editGramsOrAmount.setText("1");
                saveButton.setVisibility(View.VISIBLE);
        });

    }

    private void setupPortionUnitSpinner() {
        List<String> list = new ArrayList<>();
        list.add("stuk");
        list.add("gram");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editPortionOrUnitSpinner.setAdapter(dataAdapter);
        editPortionOrUnitSpinner.setSelection(0);
    }

    private void setupSpinner() {
        mealtypeSpinner = findViewById(R.id.edit_meal_type);

        List<String> list = new ArrayList<>();
        list.add("Breakfast");
        list.add("Lunch");
        list.add("Dinner");
        list.add("Snacks");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealtypeSpinner.setAdapter(dataAdapter);

        mealtypeSpinner.setSelection(0);

    }
}
