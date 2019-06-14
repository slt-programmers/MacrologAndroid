package com.example.macrologandroid;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.example.macrologandroid.services.AuthenticationService;

import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class DeleteAccountActivity extends AppCompatActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> attemptDelete());
    }

    private void attemptDelete() {
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        String password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString();

        AuthenticationService authService = new AuthenticationService();
        disposable = authService.deleteAccount(password)
                .subscribe(res -> {
                    //

                }, err -> {
                    passwordLayout.setErrorEnabled(true);
                    passwordLayout.setError("Could not delete account");
                });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
