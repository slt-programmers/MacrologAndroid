package com.example.macrologandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        EditText oldpw = findViewById(R.id.old_password);
        EditText newpw = findViewById(R.id.new_password);
        EditText confirm_password = findViewById(R.id.confirm_password);

        Button changeButton = findViewById(R.id.change_button);
        changeButton.setOnClickListener(v -> {

        });
    }
}
