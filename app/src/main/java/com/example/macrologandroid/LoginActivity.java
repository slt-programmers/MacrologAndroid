package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.macrologandroid.dtos.AuthenticationResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.services.AuthenticationService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


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

    private OnLoggedInListener callback;

    public void setOnLoggedInListener(MainActivity activity) {
        callback = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setOnLoggedInListener(MainActivity.getInstance());

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
        // Set up the login form.
        mUserOrEmailView = findViewById(R.id.user_email);
        mPasswordView = findViewById(R.id.password);

        mPasswordView.setTypeface(Typeface.DEFAULT);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        mLoginResultView = findViewById(R.id.login_result);

        Button mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(view -> attemptLogin());

        mNewUsernameView = findViewById(R.id.register_username);
        mNewEmailView = findViewById(R.id.register_email);
        mNewPasswordView = findViewById(R.id.register_password);

        mNewPasswordView.setTypeface(Typeface.DEFAULT);
        mNewPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        mRegisterResultView = findViewById(R.id.register_result);

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(v -> attemptRegister());

        authService = new AuthenticationService();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(LoginActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    private void attemptLogin() {
        resetErrors();
        mUserOrEmailView.setError(null);
        mPasswordView.setError(null);

        String username = mUserOrEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isUsernameValid(username)) {
            mUserOrEmailView.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailView;
            cancel = true;
        }

        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            if (focusView == null) {
                focusView = mPasswordView;
            }
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            login(username, password);
        }
    }

    private void attemptRegister() {
        resetErrors();

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

    private void resetErrors() {
        mLoginResultView.setVisibility(View.GONE);
        mRegisterResultView.setVisibility(View.GONE);
    }

    private boolean isUsernameValid(String username) {
        return !TextUtils.isEmpty(username);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {

        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    @SuppressLint("CheckResult")
    private void login(String username, String password) {
        authService.authenticate(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {saveCredentials(res); notifyDiaryFragment(); finish();},
                        err -> {
                    mLoginResultView.setText(R.string.login_failed);
                    mLoginResultView.setVisibility(View.VISIBLE);
                }
                );

    }

    private void notifyDiaryFragment() {
        callback.updatePage();
    }

    @SuppressLint("CheckResult")
    private void register(String username, String email, String password) {
        authService.register(username, email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {saveCredentials(res); finish();},
                        err -> {
                            mRegisterResultView.setText(((HttpException) err).response().errorBody().string());
                            mRegisterResultView.setVisibility(View.VISIBLE);
                        });
    }

    private void saveCredentials(AuthenticationResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }

    public interface OnLoggedInListener {
        void updatePage();
    }

}

