package com.senderman.lastkatkabot.service;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

@Service
public class GsonSerializer implements Serializer {

    private final Gson gson;

    public GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String serialize(Object object) {
        return gson.toJson(object);
    }

    @Override
    public <T> T deserialize(String data, Class<T> tClass) {
        return gson.fromJson(data, tClass);
    }
}
