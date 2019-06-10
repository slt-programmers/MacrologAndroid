package com.example.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.BuildConfig;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.dtos.SettingsResponse;
import com.example.macrologandroid.dtos.UserSettingsResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public class UserService extends Service {

    private ApiService apiService;

    public UserService() {
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(client.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(CustomGsonConverter.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Observable<List<SettingsResponse>> getSettings() {
        return apiService.getSettings();
    }

    // Gets current weight from weight repository
    public Observable<UserSettingsResponse> getUserSettings() {
        return apiService.getUserSettings();
    }

    public Observable<ResponseBody> putSetting(SettingsResponse setting) {
        return apiService.putSetting(setting);
    }

    private interface ApiService {

        @GET("settings")
        Observable<List<SettingsResponse>> getSettings();

        @GET("settings/user")
        Observable<UserSettingsResponse> getUserSettings();

        @PUT("settings")
        Observable<ResponseBody> putSetting(@Body SettingsResponse setting);

    }
}
