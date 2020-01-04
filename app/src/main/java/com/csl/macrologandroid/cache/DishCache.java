package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.DishResponse;

import java.util.ArrayList;
import java.util.List;

public class DishCache {

    private static DishCache instance;

    private List<DishResponse> cache;

    private DishCache() {
        this.cache = new ArrayList<>();
    }

    public static DishCache getInstance() {
        if (instance == null) {
            instance = new DishCache();
        }
        return instance;
    }

    public void addToCache(List<DishResponse> dishResponses) {
        cache.addAll(dishResponses);
    }

    public List<DishResponse> getCache() {
        return cache;
    }

    public void clearCache() {
        cache = new ArrayList<>();
    }

}
