package com.csl.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.MainActivity;
import com.csl.macrologandroid.dtos.SettingsResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
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

    // Gets current weight from weight repository
    public Observable<UserSettingsResponse> getUserSettings() {
        return apiService.getUserSettings().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> putSetting(SettingsResponse setting) {
        return apiService.putSetting(setting).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @GET("settings/user")
        Observable<UserSettingsResponse> getUserSettings();

        @PUT("settings")
        Observable<ResponseBody> putSetting(@Body SettingsResponse setting);

    }
}
