package com.example.macrologandroid.Cache;

import com.example.macrologandroid.DTO.LogEntryResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryLogCache {

    private static DiaryLogCache instance;

    private Map<LocalDate, List<LogEntryResponse>> logEntryCache;

    private DiaryLogCache() {
        this.logEntryCache = new HashMap<>();
    }

    public static DiaryLogCache getInstance() {
        if (instance == null) {
            instance = new DiaryLogCache();
        }
        return instance;
    }

    public void addToCache(LocalDate date, List<LogEntryResponse> logEntryResponse) {
        logEntryCache.put(date, logEntryResponse);
    }

    public List<LogEntryResponse> getFromCache(LocalDate localDate) {
        return logEntryCache.get(localDate);
    }

}
