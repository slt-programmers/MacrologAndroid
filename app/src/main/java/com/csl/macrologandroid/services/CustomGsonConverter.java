package com.csl.macrologandroid.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.converter.gson.GsonConverterFactory;

class CustomGsonConverter {

    static GsonConverterFactory create() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return GsonConverterFactory.create(gson);
    }

}

