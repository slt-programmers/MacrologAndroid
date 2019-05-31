package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.macrologandroid.dtos.AuthenticationResponse;
import com.example.macrologandroid.fragments.UserFragment;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.services.AuthenticationService;
import com.example.macrologandroid.services.UserService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.example.macrologandroid.fragments.UserFragment.EDIT_DETAILS_ID;


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

    private static final int INTAKE_SUCCESSFUL = 678;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTAKE_SUCCESSFUL) {
            if (resultCode == Activity.RESULT_OK) {
                finishWithResult();
            }
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
        // Set up the login form.
        mUserOrEmailView = findViewById(R.id.user_email);
        mPasswordView = findViewById(R.id.password);

        mPasswordView.setTypeface(Typeface.DEFAULT);
        mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        mLoginResultView = findViewById(R.id.login_result);

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setClickable(true);
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

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
            startActivity(intent);
        }
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

        if (isUsernameInvalid(username)) {
            mUserOrEmailView.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailView;
            cancel = true;
        }

        if (isPasswordInvalid(password)) {
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

        if (isUsernameInvalid(username)) {
            mNewUsernameView.setError(getString(R.string.error_field_required));
            focusView = mNewUsernameView;
            cancel = true;
        }

        if (!isEmailValid(email)) {
            mNewEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mNewEmailView;
            cancel = true;
        }

        if (isPasswordInvalid(password)) {
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

    private boolean isUsernameInvalid(String username) {
        return TextUtils.isEmpty(username);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordInvalid(String password) {
        return TextUtils.isEmpty(password) || password.length() < 6;
    }

    @SuppressLint("CheckResult")
    private void login(String username, String password) {
        authService.authenticate(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                            saveCredentials(res);
                            finishWithResult();
                        }, err -> {
                            mLoginResultView.setText(R.string.login_failed);
                            mLoginResultView.setVisibility(View.VISIBLE);
                        }
                );

    }

    private void finishWithResult() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @SuppressLint("CheckResult")
    private void register(String username, String email, String password) {
        authService.register(username, email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                            saveCredentials(res);
                            Intent intent = new Intent(this, EditPersonalDetailsActivity.class);
                            intent.putExtra("INTAKE", true);
                            startActivityForResult(intent, INTAKE_SUCCESSFUL);
                        },
                        err -> {
                            ResponseBody body = ((HttpException) err).response().errorBody();
                            if (body != null) {
                                mRegisterResultView.setText(body.string());
                                mRegisterResultView.setVisibility(View.VISIBLE);
                            } else {
                                mRegisterResultView.setText(R.string.general_error);
                                mRegisterResultView.setVisibility(View.VISIBLE);
                            }
                        });
    }

    private void saveCredentials(AuthenticationResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }

}

