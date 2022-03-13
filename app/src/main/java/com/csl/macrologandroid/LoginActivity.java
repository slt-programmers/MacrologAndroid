package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;
import com.csl.macrologandroid.util.ResetErrorTextWatcher;

import java.net.ConnectException;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText userEmailInput;
    private EditText passwordInput;
    private TextView userEmailError;
    private TextView passwordError;

    private AuthenticationService authService;
    private static final int INTAKE_SUCCESSFUL = 678;
    private Disposable disposable;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTAKE_SUCCESSFUL && resultCode == Activity.RESULT_OK) {
            finishWithResult();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        userEmailInput = findViewById(R.id.user);
        passwordInput = findViewById(R.id.password);
        userEmailError = findViewById(R.id.user_email_error);
        passwordError = findViewById(R.id.password_error);
        userEmailInput.addTextChangedListener(new ResetErrorTextWatcher(userEmailInput, userEmailError));
        passwordInput.addTextChangedListener(new ResetErrorTextWatcher(passwordInput, passwordError));

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        Button mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(view -> attemptLogin());

        TextView mRegisterText = findViewById(R.id.register_text);
        mRegisterText.setOnClickListener(v -> {
            Intent registerIntent = new Intent(this, RegisterActivity.class);
            startActivity(registerIntent);
            finish();
        });
        authService = new AuthenticationService(getToken());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(LoginActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptLogin() {
        String username = Objects.requireNonNull(userEmailInput).getText().toString();
        String password = Objects.requireNonNull(passwordInput).getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            userEmailError.setVisibility(View.VISIBLE);
            userEmailInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            focusView = userEmailInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordError.setText(R.string.error_field_required);
            passwordError.setVisibility(View.VISIBLE);
            passwordInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            if (focusView == null) {
                focusView = passwordInput;
            }
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            login(username, password);
        }
    }

    private void login(String username, String password) {
        disposable = authService.authenticate(username, password).subscribe(res -> {
                    saveCredentials(res);
                    finishWithResult();
                }, err -> {
                    Log.e(this.getLocalClassName(), err.getMessage());
                    if (err instanceof ConnectException) {
                        passwordError.setText(R.string.connection_error);
                    } else {
                        passwordError.setText(R.string.login_failed);
                    }
                    passwordError.setVisibility(View.VISIBLE);
                }
        );

    }

    private void finishWithResult() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void saveCredentials(AuthenticationResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }


    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

}

