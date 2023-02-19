package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

@MongoRepository
public interface BlacklistedUserRepository extends CrudRepository<BlacklistedUser, Long> {
}
