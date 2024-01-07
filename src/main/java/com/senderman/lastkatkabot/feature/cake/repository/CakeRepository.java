package com.senderman.lastkatkabot.feature.cake.repository;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;

@JdbcRepository(dialect = Dialect.H2)
public interface CakeRepository extends CrudRepository<Cake, Integer> {

    long deleteByCreatedAtLessThan(Timestamp createdAt);

}
