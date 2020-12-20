package com.csl.macrologandroid.dtos;

import java.io.Serializable;
import java.util.Date;

public class ActivityResponse implements Serializable {

    private Long id;
    private Date day;
    private String name;
    private int calories;
    private String syncedWith;
    private Long syncedId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getSyncedWith() {
        return syncedWith;
    }

    public void setSyncedWith(String syncedWith) {
        this.syncedWith = syncedWith;
    }

    public Long getSyncedId() {
        return syncedId;
    }

    public void setSyncedId(Long syncedId) {
        this.syncedId = syncedId;
    }
}
