package com.senderman.lastkatkabot.config.repository;

import com.senderman.lastkatkabot.config.model.Settings;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

@MongoRepository
public interface SettingsRepository extends CrudRepository<Settings, String> {

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends Settings> @NonNull S update(@NonNull S entity);
}
