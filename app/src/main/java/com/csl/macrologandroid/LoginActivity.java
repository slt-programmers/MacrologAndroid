package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;

import java.net.ConnectException;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private TextInputLayout mUserOrEmailLayout;
    private TextInputLayout mPasswordLayout;
    private TextView mLoginResultView;

    private TextInputLayout mNewUsernameView;
    private TextInputLayout mNewEmailView;
    private TextInputLayout mNewPasswordView;
    private TextView mRegisterResultView;

    private AuthenticationService authService;

    private static final int INTAKE_SUCCESSFUL = 678;
    private Disposable disposable;

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

        mUserOrEmailLayout = findViewById(R.id.user_email);
        mPasswordLayout = findViewById(R.id.password);

        // For hiding password with dots
        Objects.requireNonNull(mPasswordLayout.getEditText()).setTypeface(Typeface.DEFAULT);
        mPasswordLayout.getEditText().setTransformationMethod(new PasswordTransformationMethod());

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

        // For hiding password with dots
        Objects.requireNonNull(mNewPasswordView.getEditText()).setTypeface(Typeface.DEFAULT);
        mNewPasswordView.getEditText().setTransformationMethod(new PasswordTransformationMethod());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptLogin() {
        resetErrors();
        mUserOrEmailLayout.setError(null);
        mPasswordLayout.setError(null);

        String username = Objects.requireNonNull(mUserOrEmailLayout.getEditText()).getText().toString();
        String password = Objects.requireNonNull(mPasswordLayout.getEditText()).getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (isUsernameInvalid(username)) {
            mUserOrEmailLayout.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailLayout.getEditText();
            cancel = true;
        }

        if (isPasswordInvalid(password)) {
            mPasswordLayout.setError(getString(R.string.error_invalid_password));
            if (focusView == null) {
                focusView = mPasswordLayout;
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

        String username = Objects.requireNonNull(mNewUsernameView.getEditText()).getText().toString();
        String email = Objects.requireNonNull(mNewEmailView.getEditText()).getText().toString();
        String password = Objects.requireNonNull(mNewPasswordView.getEditText()).getText().toString();

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

    private void login(String username, String password) {
        disposable = authService.authenticate(username, password)
                .subscribe(res -> {
                            saveCredentials(res);
                            finishWithResult();
                        }, err -> {
                            Log.d(this.getLocalClassName(), err.getMessage());
                            if (err instanceof ConnectException) {
                                mLoginResultView.setText(R.string.connection_error);
                                mLoginResultView.setVisibility(View.VISIBLE);
                            } else {
                                mLoginResultView.setText(R.string.login_failed);
                                mLoginResultView.setVisibility(View.VISIBLE);
                            }
                        }
                );

    }

    private void finishWithResult() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void register(String username, String email, String password) {
        disposable = authService.register(username, email, password)
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

