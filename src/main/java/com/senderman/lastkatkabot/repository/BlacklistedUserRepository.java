package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import org.springframework.data.repository.CrudRepository;

public interface BlacklistedUserRepository extends CrudRepository<BlacklistedUser, Integer> {
}
