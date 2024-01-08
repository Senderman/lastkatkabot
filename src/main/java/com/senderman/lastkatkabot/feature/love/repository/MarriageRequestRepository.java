package com.senderman.lastkatkabot.feature.love.repository;

import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;

@JdbcRepository(dialect = Dialect.H2)
public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    long deleteByCreatedAtLessThan(Timestamp createdAt);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

}
