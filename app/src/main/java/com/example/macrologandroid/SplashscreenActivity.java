package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.macrologandroid.Lifecycle.Session;
import com.example.macrologandroid.Services.HealthcheckService;

import java.net.SocketTimeoutException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.HttpException;

public class SplashscreenActivity extends AppCompatActivity {

    private HealthcheckService service;
    private String token;
    private int callCounter = 0;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        service = new HealthcheckService();
        token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);

        ImageView image = findViewById(R.id.animated_image);
        image.setBackgroundResource(R.drawable.hamster_wheel);
        AnimatedVectorDrawable animation = (AnimatedVectorDrawable) image.getBackground();
        animation.start();

        Session.getInstance().resetTimestamp();

        doHealthCheck();
    }

    @SuppressLint("CheckResult")
    private void doHealthCheck() {
        callCounter++;
        service.healthcheck(token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            startActivity(new Intent(SplashscreenActivity.this, MainActivity.class));
                            finish();
                        },
                        err -> {
                            if (err instanceof SocketTimeoutException && callCounter < 3) {
                                doHealthCheck();
                            }
                            else {
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("TOKEN_EXPIRED", true);
                                startActivity(intent);
                                finish();
                            }
                        });


    }
}
