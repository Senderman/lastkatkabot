package com.senderman.lastkatkabot.util;

public interface Serializer {

    String serialize(Object object);

    <T> T deserialize(String data, Class<T> tClass);

}
