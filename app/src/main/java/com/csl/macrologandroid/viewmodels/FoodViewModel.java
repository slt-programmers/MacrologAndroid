package com.csl.macrologandroid.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.services.FoodRepository;

import java.util.List;

public class FoodViewModel extends ViewModel {

    private FoodRepository foodRepository;

    private final LiveData<List<Food>> foodListData;

    public FoodViewModel(String token) {
        foodRepository = new FoodRepository(token);
        foodListData = foodRepository.getAllFood();
    }

    public LiveData<List<Food>> getFoodListData() {
        return foodListData;
    }

    public void postFood(Food food) {
        foodRepository.postFood(food);
    }

    public LiveData<Boolean> getFoodPostResult() {

    }

}
