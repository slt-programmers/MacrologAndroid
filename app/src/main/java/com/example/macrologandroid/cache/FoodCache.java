package com.example.macrologandroid.cache;

import com.example.macrologandroid.dtos.FoodResponse;
import com.example.macrologandroid.dtos.LogEntryResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodCache {

    private static FoodCache instance;

    private List<FoodResponse> foodCache;

    private FoodCache() {
        this.foodCache = new ArrayList<>();
    }

    public static FoodCache getInstance() {
        if (instance == null) {
            instance = new FoodCache();
        }
        return instance;
    }

    public void addToCache(List<FoodResponse> foodResponses) {
        foodCache.addAll(foodResponses);
    }

    public List<FoodResponse> getCache() {
        return foodCache;
    }

    public void clearCache() {
        foodCache = new ArrayList<>();
    }

}
