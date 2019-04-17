package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

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
    private Boolean expired;
    private Handler handler;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        service = new HealthcheckService();
        token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);

        ImageView image = findViewById(R.id.animated_image);
        TextView waitMessage = findViewById(R.id.wait_message);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                image.setBackgroundResource(R.drawable.hamster_wheel);
                AnimatedVectorDrawable animation = (AnimatedVectorDrawable) image.getBackground();
                animation.start();
                waitMessage.setVisibility(View.VISIBLE);
            }
        }, 2000);

        Intent intent = getIntent();
        expired = (Boolean) intent.getSerializableExtra("SESSION_EXPIRED");

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
                            if (expired) {
                                finish();
                            } else {
                                startActivity(new Intent(SplashscreenActivity.this, MainActivity.class));
                                finish();
                            }
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
