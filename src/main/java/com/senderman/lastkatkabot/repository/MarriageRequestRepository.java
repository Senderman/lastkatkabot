package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.MarriageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MarriageRequestRepository extends CrudRepository<MarriageRequest, Integer> {

    Optional<MarriageRequest> findFirstByOrderByIdDesc();

    void deleteByRequestDateLessThan(int requestDate);

    void deleteByProposerIdOrProposeeId(int proposerId, int proposeeId);

}
