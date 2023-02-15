package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.AdminUser;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {
}
