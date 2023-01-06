package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.ActivityResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityCache {

    private static ActivityCache instance;

    private Map<Date, List<ActivityResponse>> cache;

    private ActivityCache() {
        this.cache = new HashMap<>();
    }

    public static ActivityCache getInstance() {
        if (instance == null) {
            instance = new ActivityCache();
        }
        return instance;
    }

    public void addToCache(Date date, List<ActivityResponse> activityResponses) {
        cache.put(date, activityResponses);
    }

    public List<ActivityResponse> getFromCache(Date date) {
        return cache.get(date);
    }

    public void removeFromCache(Date date) {
        cache.put(date, null);
    }

    public void clearCache() {
        cache = new HashMap<>();
    }

}
