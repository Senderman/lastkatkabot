package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BlacklistedUserRepository extends MongoRepository<BlacklistedUser, Integer> {
}
