package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.EntryDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.util.DateParser;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class EntryService {

    private final ApiService apiService;

    public EntryService(String token) {
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

    public Observable<List<LogEntryResponse>> getLogsForDay(Date date) {
        return apiService.getLogsForDay(DateParser.format(date)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<LogEntryResponse>> postEntries(List<EntryDto> entries, Date date) {
        return apiService.postEntries(entries, DateParser.format(date)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @GET("logs/day/{date}")
        Observable<List<LogEntryResponse>> getLogsForDay(@Path("date") String date);

        @POST("logs/day/{date}")
        Observable<List<LogEntryResponse>> postEntries(@Body List<EntryDto> entries,
                                                       @Path("date") String date);

    }
}
