package com.csl.macrologandroid.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import retrofit2.converter.gson.GsonConverterFactory;

public class CustomGsonConverter {

    public static GsonConverterFactory create() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
//                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        return GsonConverterFactory.create(gson);
    }

}

//class LocalDateAdapter implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {
//
//    @Override
//    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
//        return LocalDate.parse(json.getAsString());
//    }
//
//    @Override
//    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
//        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE));
//    }
//}
