package com.senderman.lastkatkabot.config.repository;

import com.senderman.lastkatkabot.config.model.Settings;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.H2)
public interface SettingsRepository extends CrudRepository<Settings, String> {

}
