package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import io.micronaut.data.repository.CrudRepository;

public interface BlacklistedUserRepository extends CrudRepository<BlacklistedUser, Long> {
}
