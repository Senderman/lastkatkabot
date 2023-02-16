package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.MarriageRequest;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    Optional<MarriageRequest> findFirstOrderByIdDesc();

    long deleteByRequestDateLessThan(int requestDate);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

}
