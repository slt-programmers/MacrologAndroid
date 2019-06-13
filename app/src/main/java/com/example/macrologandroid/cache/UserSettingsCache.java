package com.example.macrologandroid.cache;

import com.example.macrologandroid.dtos.UserSettingsResponse;

public class UserSettingsCache {

    private static UserSettingsCache instance;

    private UserSettingsResponse userSettingsCache;

    private UserSettingsCache() {
        this.userSettingsCache = null;
    }

    public static UserSettingsCache getInstance() {
        if (instance == null) {
            instance = new UserSettingsCache();
        }
        return instance;
    }

    public UserSettingsResponse getCache() {
        return userSettingsCache;
    }

    public void updateCache(UserSettingsResponse settingsResponse) {
        userSettingsCache = settingsResponse;
    }

    public void clearCache() {
        userSettingsCache = null;
    }

}
