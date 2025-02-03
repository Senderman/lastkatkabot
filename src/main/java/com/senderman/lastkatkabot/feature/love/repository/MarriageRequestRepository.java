package com.senderman.lastkatkabot.feature.love.repository;

import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    void deleteByCreatedAtLessThan(LocalDateTime createdAt);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

    @Query("SELECT MAX(id) +1 FROM marriage_request;")
    Optional<Integer> getLowestAvailableId();

}
