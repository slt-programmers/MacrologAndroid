package com.example.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.BuildConfig;
import com.example.macrologandroid.dtos.AuthenticationRequest;
import com.example.macrologandroid.dtos.AuthenticationResponse;
import com.example.macrologandroid.dtos.ChangePasswordRequest;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AuthenticationService extends Service {

    private ApiService apiService;

    public AuthenticationService() {
         Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL + "api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // The username field is used for both username and email when logging in
    // This is handled properly by the backend
    public Observable<AuthenticationResponse> authenticate(String username, String password) {
        return apiService.authenticate(new AuthenticationRequest(username, "",password));
    }

    public Observable<AuthenticationResponse> register(String username, String email, String password) {
        return apiService.register((new AuthenticationRequest(username, email, password)));
    }

    public Observable<ResponseBody> changePassword(String oldPassword, String newPassword, String confirmNew) {
        return apiService.changePassword(new ChangePasswordRequest(oldPassword, newPassword, confirmNew));
    }

    public Observable<ResponseBody> resetPassword(String email) {
        return apiService.resetPassword(email);
    }

    private interface ApiService {

        @POST("authenticate")
        Observable<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);

        @POST("signup")
        Observable<AuthenticationResponse> register(@Body AuthenticationRequest request);

        @POST("changePassword")
        Observable<ResponseBody> changePassword(@Body ChangePasswordRequest request);

        @POST("resetPassword")
        Observable<ResponseBody> resetPassword(@Body String email);

    }
}
