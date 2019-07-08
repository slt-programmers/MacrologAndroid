package com.csl.macrologandroid;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.csl.macrologandroid.services.AuthenticationService;

import io.reactivex.disposables.Disposable;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private Button sendButton;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        emailEditText = findViewById(R.id.email_edittext);
        emailEditText.addTextChangedListener(textWatcher);

        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(v -> attemptSend());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptSend() {
        Editable editable = emailEditText.getText();
        if (editable != null) {
            String email = emailEditText.getText().toString();
            if (!email.contains("@")) {
                emailEditText.setError(getResources().getString(R.string.error_invalid_email));
            } else {
                AuthenticationService authService = new AuthenticationService(getToken());
                disposable = authService.resetPassword(email)
                        .subscribe(
                                res -> finish(),
                                err -> emailEditText.setError(err.getMessage())
                        );
            }
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            sendButton.setEnabled(!(s == null || s.toString().isEmpty()));
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    };


    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
