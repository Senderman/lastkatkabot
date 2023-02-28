package com.senderman.lastkatkabot.feature.access.repository;

import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@MongoRepository
public interface BlacklistedUserRepository extends CrudRepository<BlacklistedUser, Long> {

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends BlacklistedUser> S update(@Valid @NotNull S entity);
}
