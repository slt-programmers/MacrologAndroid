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

import com.example.macrologandroid.Models.AuthenticationResponse;
import com.example.macrologandroid.Services.AuthenticationService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mUserOrEmailView;
    private EditText mPasswordView;
    private TextView mLoginResultView;

    private EditText mNewUsernameView;
    private EditText mNewEmailView;
    private EditText mNewPasswordView;
    private TextView mRegisterResultView;

    private AuthenticationService authService;

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

        Button mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mNewUsernameView = findViewById(R.id.register_username);
        mNewEmailView = findViewById(R.id.register_email);
        mNewPasswordView = findViewById(R.id.register_password);
        mRegisterResultView = findViewById(R.id.register_result);

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        authService = new AuthenticationService();

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        mUserOrEmailView.setError(null);
        mPasswordView.setError(null);

        String username = mUserOrEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!isUsernameValid(username)) {
            mUserOrEmailView.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            login(username, password);
        }
    }

    private void attemptRegister() {
        mNewUsernameView.setError(null);
        mNewEmailView.setError(null);
        mNewPasswordView.setError(null);

        String username = mNewUsernameView.getText().toString();
        String email = mNewEmailView.getText().toString();
        String password = mNewPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isUsernameValid(username)) {
            mNewUsernameView.setError(getString(R.string.error_field_required));
            focusView = mNewUsernameView;
            cancel = true;
        }

        if (!isEmailValid(email)) {
            mNewEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mNewEmailView;
            cancel = true;
        }

        if (!isPasswordValid(password)) {
            mNewPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mNewPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            register(username, email, password);
        }

    }

    private boolean isUsernameValid(String username) {
        return !TextUtils.isEmpty(username) && username.length() >= 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password);
    }

    @SuppressLint("CheckResult")
    private void login(String username, String password) {
        authService.authenticate(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {saveCredentials(res); finish();},
                        err -> mLoginResultView.setText(err.toString())
                );

    }

    @SuppressLint("CheckResult")
    private void register(String username, String email, String password) {
        authService.register(username, email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {saveCredentials(res); finish();},
                        err -> mRegisterResultView.setText(err.toString()));
    }

    private void saveCredentials(AuthenticationResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }

}

