package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.LogEntryResponse;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryLogCache {

    private static DiaryLogCache instance;

    private Map<Date, List<LogEntryResponse>> logEntryCache;

    private DiaryLogCache() {
        this.logEntryCache = new HashMap<>();
    }

    public static DiaryLogCache getInstance() {
        if (instance == null) {
            instance = new DiaryLogCache();
        }
        return instance;
    }

    public void addToCache(Date date, List<LogEntryResponse> logEntryResponse) {
        logEntryCache.put(date, logEntryResponse);
    }

    public List<LogEntryResponse> getFromCache(Date date) {
        return logEntryCache.get(date);
    }

    public void removeFromCache(Date date) {
        logEntryCache.put(date, null);
    }

    public void clearCache() {
        logEntryCache = new HashMap<>();
    }

}
