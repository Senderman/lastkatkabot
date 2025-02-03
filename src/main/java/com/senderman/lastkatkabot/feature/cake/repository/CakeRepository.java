package com.senderman.lastkatkabot.feature.cake.repository;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface CakeRepository extends CrudRepository<Cake, Integer> {

    void deleteByCreatedAtLessThan(LocalDateTime createdAt);

    @Query("SELECT MAX(id) +1 FROM cake;")
    Optional<Integer> getLowestAvailableId();

}
