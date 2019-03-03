package com.example.macrologandroid.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.macrologandroid.Models.LoginRequest;
import com.example.macrologandroid.Models.LoginResponse;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AuthenticationService {

    private ApiService apiService;

    public AuthenticationService() {
         Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://macrolog-backend.herokuapp.com/api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Observable<LoginResponse>  authenticate(String username, String password) {
        return  apiService.authenticate(new LoginRequest(username, password));
    }

    private interface ApiService {

        @POST("authenticate")
        Observable<LoginResponse> authenticate(@Body LoginRequest request);

    }
}
