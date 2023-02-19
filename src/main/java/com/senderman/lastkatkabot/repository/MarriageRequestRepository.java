package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.MarriageRequest;
import io.micronaut.data.mongodb.annotation.MongoFindOptions;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    @MongoFindQuery(value = "{}", sort = "{ _id: -1 }")
    @MongoFindOptions(limit = 1)
    Optional<MarriageRequest> findFirstOrderByIdDesc();

    long deleteByRequestDateLessThan(int requestDate);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

}
