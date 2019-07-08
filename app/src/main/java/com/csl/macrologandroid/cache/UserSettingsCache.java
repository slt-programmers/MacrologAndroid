package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.UserSettingsResponse;

public class UserSettingsCache {

    private static UserSettingsCache instance;

    private UserSettingsResponse cache;

    private UserSettingsCache() {
        this.cache = null;
    }

    public static UserSettingsCache getInstance() {
        if (instance == null) {
            instance = new UserSettingsCache();
        }
        return instance;
    }

    public UserSettingsResponse getCache() {
        return cache;
    }

    public void updateCache(UserSettingsResponse settingsResponse) {
        cache = settingsResponse;
    }

    public void clearCache() {
        cache = null;
    }

}
