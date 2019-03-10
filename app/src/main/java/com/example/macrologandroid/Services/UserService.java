package com.example.macrologandroid.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.DTO.UserSettingResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    public Observable<ResponseBody> putSetting(UserSettingResponse setting) {
        return apiService.putSetting(setting);
    }

    private interface ApiService {

        @GET("settings")
        Observable<List<UserSettingResponse>> getSettings();

        @PUT("settings")
        Observable<ResponseBody> putSetting(@Body UserSettingResponse setting);

    }
}
