package com.example.macrologandroid.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.Models.LoginRequest;
import com.example.macrologandroid.Models.LoginResponse;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class HealthcheckService {

    private HealthcheckService.ApiService apiService;

    public HealthcheckService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://macrolog-backend.herokuapp.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(HealthcheckService.ApiService.class);
    }

    public Observable<Boolean>  healthcheck(String token) {
        if (token != null) {
            return apiService.healthcheck("Bearer " + token);
        } else {
            return apiService.healthcheck("");
        }
    }

    private interface ApiService {

        @GET("healthcheck")
        Observable<Boolean> healthcheck(@Header("Authorization") String authorization);

    }
}
