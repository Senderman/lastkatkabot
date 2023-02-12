package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.MarriageRequest;

import java.util.Optional;

public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    Optional<MarriageRequest> findFirstByOrderByIdDesc();

    long deleteByRequestDateLessThan(int requestDate);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

}
