package com.csl.macrologandroid.util;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.widget.AppCompatTextView;

import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SpinnerSetupUtil {

    public FoodResponse getFoodFromList(String foodname, List<FoodResponse> allFood) {
        for (FoodResponse food : allFood) {
            if (food.getName().trim().equals(foodname.trim())) {
                return food;
            }
        }
        return null;
    }

    private List<String> getPortionList(FoodResponse selectedFood) {
        List<String> list = new ArrayList<>();
        for (PortionResponse portion : selectedFood.getPortions()) {
            String desc = portion.getDescription();
            if (desc != null && !desc.isEmpty()) {
                list.add(desc);
            }
        }
        list.add("gram");
        return list;
    }

    public void setupPortionUnitSpinner(Context context, FoodResponse selectedFood, Spinner editPortionOrUnitSpinner, TextInputEditText editGramsOrAmount) {
        List<String> list = getPortionList(selectedFood);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, list);
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
                // Not needed
            }
        });

        editGramsOrAmount.setVisibility(View.VISIBLE);
    }

}
