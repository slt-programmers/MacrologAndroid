package com.example.macrologandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.macrologandroid.DTO.LogEntryResponse;

import java.util.List;

public class EditLogEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log_entry);

        List<LogEntryResponse> entries = (List<LogEntryResponse>) getIntent().getSerializableExtra("logentries");

        Button backbutton = findViewById(R.id.backbutton);
        backbutton.setOnClickListener(v -> {
            finish();
        });
    }
}
