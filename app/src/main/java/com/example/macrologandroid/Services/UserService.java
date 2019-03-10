package com.example.macrologandroid.Services;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.DTO.UserSettingResponse;

import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public class UserService extends Service {

    private String authHeader;
    
    private ApiService apiService;

    public UserService() {
        String token = MainActivity.getPreferences().getString("TOKEN", "");
        authHeader = "Authorization: Bearer " + token;
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
                .baseUrl("https://macrolog-backend.herokuapp.com/")
                .client(client.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public Observable<List<UserSettingResponse>> getSettings() {
        return apiService.getSettings();
    }
    
    public Observable<ResponseEntity> putSetting(UserSettingResponse setting) {
        return apiService.putSetting(setting);
    }

    private interface ApiService {

        @GET("settings")
        Observable<List<UserSettingResponse>> getSettings();
        
        @PUT("settings")
        Observable<ResponseEntity> putSetting(@Body UserSettingResponse setting);
    
    }
}
