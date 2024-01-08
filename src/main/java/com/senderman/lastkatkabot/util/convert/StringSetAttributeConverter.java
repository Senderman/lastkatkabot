package com.senderman.lastkatkabot.util.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.senderman.lastkatkabot.util.Serializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

import java.util.Set;

@Singleton
public class StringSetAttributeConverter implements AttributeConverter<Set<String>, String> {

    private final Serializer serializer;

    public StringSetAttributeConverter(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public @Nullable String convertToPersistedValue(@Nullable Set<String> entityValue, @NonNull ConversionContext context) {
        return entityValue == null ? null : serializer.serialize(entityValue);
    }

    @Override
    public @Nullable Set<String> convertToEntityValue(@Nullable String persistedValue, @NonNull ConversionContext context) {
        return persistedValue == null ? null : serializer.deserialize(persistedValue, new TypeReference<>() {
        });
    }
}
