package com.example.macrologandroid.Services;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.Models.UserSetting;

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
import retrofit2.http.GET;
import retrofit2.http.Headers;

public class UserService extends Service {

    private ApiService apiService;

    public UserService() {
        String token = MainActivity.getPreferences().getString("TOKEN", "");
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Autorization", "Bearer " + token)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://macrolog-backend.herokuapp.com/")
//                .client(client.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public Observable<List<UserSetting>> getSettings() {
        return apiService.getSettings();
    }


    private interface ApiService {
        @GET("settings")
        @Headers("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2Vycy9Uek1Vb2NNRjRwIiwiZXhwIjoxNTUyOTIzODk0LCJuYW1lIjoiQ2FybWVuIiwidXNlcklkIjoyfQ.Qk5wE9p-S5jZxJUlCbZ-tUV6ZGoV_qMZdVEnCbQWLFE")
        Observable<List<UserSetting>> getSettings();
    }
}
