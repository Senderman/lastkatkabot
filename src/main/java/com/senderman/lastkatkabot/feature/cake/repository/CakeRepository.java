package com.senderman.lastkatkabot.feature.cake.repository;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface CakeRepository extends CrudRepository<Cake, Integer> {

    void deleteByCreatedAtLessThan(Timestamp createdAt);

    @Query("SELECT MAX(ID) +1 FROM CAKE;")
    Optional<Integer> getLowestAvailableId();

}
