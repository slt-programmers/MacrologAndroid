package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.services.AuthenticationService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordView;
    private EditText newPasswordView;
    private EditText confirmPasswordView;

    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordView = findViewById(R.id.old_password);
        oldPasswordView.setTypeface(Typeface.DEFAULT);
        oldPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        newPasswordView = findViewById(R.id.new_password);
        newPasswordView.setTypeface(Typeface.DEFAULT);
        newPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        confirmPasswordView = findViewById(R.id.confirm_password);
        confirmPasswordView.setTypeface(Typeface.DEFAULT);
        confirmPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        errorTextView = findViewById(R.id.error_text);
        errorTextView.setVisibility(View.GONE);

        Button backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        Button changeButton = findViewById(R.id.change_button);
        changeButton.setOnClickListener(v -> {
            changePassword();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(ChangePasswordActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    @SuppressLint("CheckResult")
    private void changePassword() {
        errorTextView.setText("");
        errorTextView.setVisibility(View.GONE);

        String newPassword = newPasswordView.getText().toString().trim();
        String confirmPassword = confirmPasswordView.getText().toString().trim();

        String oldPassword = oldPasswordView.getText().toString();
        if (newPassword.equals(confirmPassword)) {
            if (!oldPassword.equals(newPassword)) {
                AuthenticationService service = new AuthenticationService();
                service.changePassword(oldPassword, newPassword, confirmPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> finish(),
                        err -> {
                            errorTextView.setText(R.string.old_password_incorrect);
                            errorTextView.setVisibility(View.VISIBLE);
                        });

            } else {
                errorTextView.setText(R.string.old_same_as_new);
                errorTextView.setVisibility(View.VISIBLE);
            }
        } else {
            errorTextView.setText(R.string.must_be_same_passwords);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }
}
