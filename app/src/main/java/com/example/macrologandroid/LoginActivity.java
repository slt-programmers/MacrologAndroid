package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.macrologandroid.Models.LoginResponse;
import com.example.macrologandroid.Services.AuthenticationService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mUserOrEmailView;
    private EditText mPasswordView;
    private TextView mLoginResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
        // Set up the login form.
        mUserOrEmailView = findViewById(R.id.user_email);
        mPasswordView = findViewById(R.id.password);
        mLoginResultView = findViewById(R.id.login_result);

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserOrEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserOrEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserOrEmailView.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            login(username, password);
        }
    }

    @SuppressLint("CheckResult")
    private void login(String username, String password) {
        AuthenticationService authService = new AuthenticationService();
        authService.authenticate(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {saveCredentials(res); finish();},
                        err -> mLoginResultView.setText(err.toString())
                );

    }

    private void saveCredentials(LoginResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }

}

