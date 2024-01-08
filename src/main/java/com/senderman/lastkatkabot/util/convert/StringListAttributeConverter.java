package com.senderman.lastkatkabot.util.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.senderman.lastkatkabot.util.Serializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class StringListAttributeConverter implements AttributeConverter<List<String>, String> {

    private final Serializer serializer;

    public StringListAttributeConverter(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public @Nullable String convertToPersistedValue(@Nullable List<String> entityValue, @NonNull ConversionContext context) {
        return entityValue == null ? null : serializer.serialize(entityValue);
    }

    @Override
    public @Nullable List<String> convertToEntityValue(@Nullable String persistedValue, @NonNull ConversionContext context) {
        return persistedValue == null ? null : serializer.deserialize(persistedValue, new TypeReference<>() {
        });
    }
}
