package com.senderman.lastkatkabot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JacksonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JacksonSerializer(@Qualifier("jsonMapper") ObjectMapper objectMapper) {
       this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> tClass) {
        try {
            return objectMapper.readValue(data, tClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
