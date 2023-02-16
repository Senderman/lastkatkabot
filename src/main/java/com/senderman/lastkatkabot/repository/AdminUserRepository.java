package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.AdminUser;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

@MongoRepository
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {
}
