package com.csl.macrologandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        Button getStartedButton = findViewById(R.id.get_started);
        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditPersonalDetailsActivity.class);
            intent.putExtra("INTAKE", true);
            startActivity(intent);
//            finish();
        });
    }
}