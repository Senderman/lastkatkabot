package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.AdminUser;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@MongoRepository
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends AdminUser> S update(@Valid @NotNull S entity);
}
