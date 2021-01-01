package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;
import java.util.Objects;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private EditText mNewUsernameView;
    private EditText mNewEmailView;
    private EditText mNewPasswordView;
    private TextView mRegisterResultView;

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
        setContentView(R.layout.activity_register);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        mNewUsernameView = findViewById(R.id.register_username);
        mNewEmailView = findViewById(R.id.register_email);
        mNewPasswordView = findViewById(R.id.register_password);

        // For hiding password with dots
        Objects.requireNonNull(mNewPasswordView).setTypeface(Typeface.DEFAULT);
        mNewPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        mRegisterResultView = findViewById(R.id.register_result);
        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(v -> attemptRegister());

        TextView loginText = findViewById(R.id.login_text);
        loginText.setOnClickListener(v -> {
            toLogin();
        });

        authService = new AuthenticationService(getToken());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(RegisterActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }


    private void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity((intent));
        finish();
    }
    @Override
    public void onBackPressed() {
        toLogin();
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptRegister() {
        resetErrors();
        mNewUsernameView.setError(null);
        mNewEmailView.setError(null);
        mNewPasswordView.setError(null);

        String username = Objects.requireNonNull(mNewUsernameView).getText().toString();
        String email = Objects.requireNonNull(mNewEmailView).getText().toString();
        String password = Objects.requireNonNull(mNewPasswordView).getText().toString();

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
        mRegisterResultView.setVisibility(View.INVISIBLE);
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

                            if (err instanceof HttpException &&
                                    ((HttpException) err).response().errorBody() != null) {
                                mRegisterResultView.setText(Objects.requireNonNull(((HttpException) err).response().errorBody()).string());
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

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

}

