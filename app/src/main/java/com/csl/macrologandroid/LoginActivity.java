package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;
import java.net.ConnectException;
import java.util.Objects;
import io.reactivex.disposables.Disposable;

public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mUserOrEmailLayout;
    private EditText mPasswordLayout;
    private TextView mLoginResultView;

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

        mUserOrEmailLayout = findViewById(R.id.user_email);
        mPasswordLayout = findViewById(R.id.password);

        // For hiding password with dots
        Objects.requireNonNull(mPasswordLayout).setTypeface(Typeface.DEFAULT);
        mPasswordLayout.setTransformationMethod(new PasswordTransformationMethod());
        mLoginResultView = findViewById(R.id.login_result);

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setClickable(true);
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
        resetErrors();
        mUserOrEmailLayout.setError(null);
        mPasswordLayout.setError(null);

        String username = Objects.requireNonNull(mUserOrEmailLayout).getText().toString();
        String password = Objects.requireNonNull(mPasswordLayout).getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (isUsernameInvalid(username)) {
            mUserOrEmailLayout.setError(getString(R.string.error_field_required));
            focusView = mUserOrEmailLayout;
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

    private void resetErrors() {
        mLoginResultView.setVisibility(View.INVISIBLE);
    }

    private boolean isUsernameInvalid(String username) {
        return TextUtils.isEmpty(username);
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
                            Log.e(this.getLocalClassName(), err.getMessage());
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

