package com.senderman.lastkatkabot.feature.access.repository;

import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {

}
