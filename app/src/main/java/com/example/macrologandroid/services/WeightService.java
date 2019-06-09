package com.example.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.macrologandroid.BuildConfig;
import com.example.macrologandroid.MainActivity;
import com.example.macrologandroid.dtos.WeightRequest;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class WeightService extends Service {

    private ApiService apiService;

    private String token;

    public WeightService() {
        token = MainActivity.getPreferences().getString("TOKEN", "");
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
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Observable<List<WeightRequest>> getAllMeasurements() {
        return apiService.getAllMeasurements();
    }

    public Observable<ResponseBody> postMeasurement(WeightRequest weightRequest) {
        return apiService.postMeasurement(weightRequest);
    }

    public Observable<ResponseBody> deleteMeasurement(int id){
        return apiService.deleteMeasurement(id);
    }

    private interface ApiService {

        @GET("weight")
        Observable<List<WeightRequest>> getAllMeasurements();

        @POST("weight")
        Observable<ResponseBody> postMeasurement(@Body WeightRequest weightRequest);

        @DELETE("weight")
        Observable<ResponseBody> deleteMeasurement(@Path("id") int id);

    }
}
