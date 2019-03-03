package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.macrologandroid.Services.HealthcheckService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.HttpException;

public class SplashscreenActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        HealthcheckService service = new HealthcheckService();

        String token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);

        service.healthcheck(token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            startActivity(new Intent(SplashscreenActivity.this, MainActivity.class));
                            finish();
                        },
                        err -> {
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("TOKEN_EXPIRED", true);
                            startActivity(intent);
                            finish();
                        });


    }
}
