package com.senderman.lastkatkabot.util;

import com.fasterxml.jackson.core.type.TypeReference;

public interface Serializer {

    String serialize(Object object);

    <T> T deserialize(String data, Class<T> tClass);

    <T> T deserialize(String data, TypeReference<T> type);

}
