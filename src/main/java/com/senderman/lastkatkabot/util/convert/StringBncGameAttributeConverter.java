package com.senderman.lastkatkabot.util.convert;

import com.senderman.lastkatkabot.feature.bnc.BncGame;
import com.senderman.lastkatkabot.util.Serializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;

@Singleton
public class StringBncGameAttributeConverter implements AttributeConverter<BncGame, String> {

    private final Serializer serializer;

    public StringBncGameAttributeConverter(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public @Nullable String convertToPersistedValue(@Nullable BncGame entityValue, @NonNull ConversionContext context) {
        return serializer.serialize(entityValue);
    }

    @Override
    public @Nullable BncGame convertToEntityValue(@Nullable String persistedValue, @NonNull ConversionContext context) {
        return serializer.deserialize(persistedValue, BncGame.class);
    }
}
