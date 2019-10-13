package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.models.Food;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class FoodCache {

    private static FoodCache instance;

    private List<Food> cache;

    private FoodCache() {
        this.cache = new ArrayList<>();
    }

    public static FoodCache getInstance() {
        if (instance == null) {
            instance = new FoodCache();
        }
        return instance;
    }

    public void addToCache(List<Food> foodResponses) {
        cache.addAll(foodResponses);
    }

    public List<Food> getCache() {
        return cache;
    }

    public void clearCache() {
        cache = new ArrayList<>();
    }

}
