package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.ConnectivityRequest;
import com.csl.macrologandroid.dtos.ConnectivityResponse;
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
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class UserService {

    private ApiService apiService;

    public UserService(String token) {
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

    // Gets current weight from weight repository
    public Observable<UserSettingsResponse> getUserSettings() {
        return apiService.getUserSettings().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> putSetting(SettingsResponse setting) {
        return apiService.putSetting(setting).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ConnectivityResponse> getConnectivitySetting(String key) {
        return apiService.getConnectivitySetting(key).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ConnectivityResponse> postConnectivitySetting(String platform, ConnectivityRequest request) {
        return apiService.postConnectivitySetting(platform, request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> deleteConnectivitySetting(String platform) {
        return apiService.deleteConnectivitySetting(platform).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @GET("settings/user")
        Observable<UserSettingsResponse> getUserSettings();

        @PUT("settings")
        Observable<ResponseBody> putSetting(@Body SettingsResponse setting);

        @GET("settings/connectivity/{key}")
        Observable<ConnectivityResponse> getConnectivitySetting(@Path("key") String key);

        @POST("settings/connectivity/{platform}")
        Observable<ConnectivityResponse> postConnectivitySetting(@Path("platform") String platform, @Body ConnectivityRequest request);

        @DELETE("settings/connectivity/{platform}")
        Observable<ResponseBody> deleteConnectivitySetting(@Path("platform") String platform);
    }
}
