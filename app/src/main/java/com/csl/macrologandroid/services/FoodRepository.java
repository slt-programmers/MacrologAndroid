package com.csl.macrologandroid.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.dtos.FoodResponse;
import com.csl.macrologandroid.dtos.PortionResponse;
import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.Portion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

@Singleton
public class FoodRepository {

    private final ApiService apiService;

    private final FoodCache foodCache = FoodCache.getInstance();

    private final MutableLiveData<List<Food>> foodData = new MutableLiveData<>();

    private Disposable disposable;

    public FoodRepository(String token) {
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

    public LiveData<List<Food>> getAllFood() {
        List<Food> cached = foodCache.getCache();
        if (cached == null || cached.isEmpty()) {
            disposable = apiService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            res -> {
                                List<Food> foodList = new ArrayList<>();
                                for (FoodResponse foodResponse : res) {
                                    foodList.add(Food.fromResponse(foodResponse));
                                }
                                foodData.setValue(foodList);
                            },
                            err -> Log.e(this.getClass().getSimpleName(), err.getMessage())
                    );
        } else {
            foodData.setValue(cached);
        }
        return foodData;
    }

    public void postFood(Food food) {
        List<PortionResponse> portionRequest = new ArrayList<>();
        for (Portion portion : food.getPortions()) {
            portionRequest.add(new PortionResponse(portion.getId(), portion.getGrams(), portion.getDescription()));
        }
        FoodResponse request = new FoodResponse(food.getId(), food.getName(), food.getProtein(), food.getFat(), food.getCarbs(), portionRequest);
        disposable = apiService.postFood(request).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                res -> {
                    FoodCache.getInstance().clearCache();
                    getAllFood();
                },
                err -> Log.e(this.getClass().getSimpleName(), err.getMessage())
        );
    }

    @Deprecated
    // Use LiveData<List<Food>> getAllFood()
    public Observable<List<FoodResponse>> getAllFoodObservable() {
        return apiService.getAlFood().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Deprecated
    // Use postFood()
    public Observable<ResponseBody> postFoodObservable(FoodResponse food) {
        return apiService.postFood(food).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private interface ApiService {

        @GET("food")
        Observable<List<FoodResponse>> getAlFood();

        @POST("food")
        Observable<ResponseBody> postFood(@Body FoodResponse food);

    }
}
