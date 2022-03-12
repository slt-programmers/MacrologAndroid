package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.LogEntryResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryLogCache {

    private static DiaryLogCache instance;

    private Map<Date, List<LogEntryResponse>> cache;

    private DiaryLogCache() {
        this.cache = new HashMap<>();
    }

    public static DiaryLogCache getInstance() {
        if (instance == null) {
            instance = new DiaryLogCache();
        }
        return instance;
    }

    public void addToCache(Date date, List<LogEntryResponse> logEntryResponse) {
        cache.put(date, logEntryResponse);
    }

    public List<LogEntryResponse> getFromCache(Date date) {
        return cache.get(date);
    }

    public void removeFromCache(Date date) {
        cache.put(date, null);
    }

    public void clearCache() {
        cache = new HashMap<>();
    }

}
