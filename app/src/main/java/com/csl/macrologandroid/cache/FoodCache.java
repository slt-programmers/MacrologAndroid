package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.FoodResponse;

import java.util.ArrayList;
import java.util.List;

public class FoodCache {

    private static FoodCache instance;

    private List<FoodResponse> cache;

    private FoodCache() {
        this.cache = new ArrayList<>();
    }

    public static FoodCache getInstance() {
        if (instance == null) {
            instance = new FoodCache();
        }
        return instance;
    }

    public void addToCache(List<FoodResponse> foodResponses) {
        cache.addAll(foodResponses);
    }

    public List<FoodResponse> getCache() {
        return cache;
    }

    public void clearCache() {
        cache = new ArrayList<>();
    }

}
