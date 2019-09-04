package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.ActivityRequest;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.util.DateParser;

import java.util.Date;
import java.util.List;

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
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ActivityService {

    private final ApiService apiService;

    public ActivityService(String token) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
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

    public Observable<List<ActivityResponse>> getActivitiesForDay(Date date) {
        return apiService.getActivitiesForDay(DateParser.format(date), true).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<ActivityResponse>> getActivitiesForDayForced(Date date) {
        return apiService.getActivitiesForDayForced(DateParser.format(date)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<ActivityResponse>> postActivity(List<ActivityRequest> activities) {
        return apiService.postActivity(activities).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> deleteActivity(long id) {
        return apiService.deleteActivity((int) id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @GET("activities/day/{date}")
        Observable<List<ActivityResponse>> getActivitiesForDay(@Path("date") String date, @Query("forced") boolean forced);

        @GET("activities/day/{date}?forceSync=true")
        Observable<List<ActivityResponse>> getActivitiesForDayForced(@Path("date") String date);

        @POST("activities")
        Observable<List<ActivityResponse>> postActivity(@Body List<ActivityRequest> entries);

        @DELETE("activities/{id}")
        Observable<ResponseBody> deleteActivity(@Path("id") int id);

    }
}
