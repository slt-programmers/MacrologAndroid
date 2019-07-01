package com.csl.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.MainActivity;
import com.csl.macrologandroid.dtos.AuthenticationRequest;
import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.dtos.ChangePasswordRequest;

import android.util.Base64;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class AuthenticationService extends Service {

    private final ApiService apiService;

    private final ApiService apiServiceWithBearer;

    public AuthenticationService() {
        String token = MainActivity.getPreferences().getString("TOKEN", "");
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL + "api/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        apiService = builder.build().create(ApiService.class);
        apiServiceWithBearer = builder.client(client.build()).build().create(ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // The username field is used for both username and email when logging in
    // This is handled properly by the backend
    public Observable<AuthenticationResponse> authenticate(String username, String password) {
        return apiService.authenticate(new AuthenticationRequest(username, "", password)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AuthenticationResponse> register(String username, String email, String password) {
        return apiService.register((new AuthenticationRequest(username, email, password))).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> changePassword(String oldPassword, String newPassword, String confirmNew) {
        return apiServiceWithBearer.changePassword(new ChangePasswordRequest(oldPassword, newPassword, confirmNew)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> resetPassword(String email) {
        return apiService.resetPassword(new AuthenticationRequest(null, email, null)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> deleteAccount(String password) {
        String encryptedPassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        return apiServiceWithBearer.deleteAccount(encryptedPassword).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @POST("authenticate")
        Observable<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);

        @POST("signup")
        Observable<AuthenticationResponse> register(@Body AuthenticationRequest request);

        @POST("changePassword")
        Observable<ResponseBody> changePassword(@Body ChangePasswordRequest request);

        @POST("resetPassword")
        Observable<ResponseBody> resetPassword(@Body AuthenticationRequest email);

        @POST("deleteAccount")
        Observable<ResponseBody> deleteAccount(@Query("password") String password);

    }
}
