package com.example.macrologandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class AddLogEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_log_entry);

        Button backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> {
            finish();
        });
    }
}
