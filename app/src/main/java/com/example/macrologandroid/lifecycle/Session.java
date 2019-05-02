package com.example.macrologandroid.lifecycle;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Session {

    private static Session instance;

    private static LocalDateTime timestamp;

    private static final long THRESHOLD = 30;

    private Session() {
        timestamp = LocalDateTime.now();
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public boolean isExpired() {
        return THRESHOLD <= ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now());
    }

    public void resetTimestamp() {
        timestamp = LocalDateTime.now();
    }

}
