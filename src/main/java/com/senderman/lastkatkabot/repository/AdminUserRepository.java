package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.AdminUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminUserRepository extends MongoRepository<AdminUser, Integer> {
}
