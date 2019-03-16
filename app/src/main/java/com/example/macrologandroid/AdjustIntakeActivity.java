package com.example.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.example.macrologandroid.Fragments.ChangeCaloriesFragment;
import com.example.macrologandroid.Fragments.ChangeMacrosFragment;

public class AdjustIntakeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_adjust_intake);
        Intent intent = getIntent();

        Button changeMacros = findViewById(R.id.button_macros);
        changeMacros.setOnClickListener(v -> {
            setFragment(new ChangeMacrosFragment());
        });

        Button changeCalories = findViewById(R.id.button_calories);
        changeCalories.setOnClickListener(v -> {
            setFragment(new ChangeCaloriesFragment());
        });

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

}
