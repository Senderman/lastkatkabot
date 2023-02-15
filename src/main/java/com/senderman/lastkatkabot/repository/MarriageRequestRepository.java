package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.MarriageRequest;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    Optional<MarriageRequest> findFirstByOrderByIdDesc();

    long deleteByRequestDateLessThan(int requestDate);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

}
