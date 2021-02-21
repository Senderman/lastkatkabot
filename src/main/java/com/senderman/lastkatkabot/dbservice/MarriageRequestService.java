package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.MarriageRequest;

import java.util.Optional;

public interface MarriageRequestService {

    Optional<MarriageRequest> findById(int id);

    void delete(MarriageRequest marriageRequest);

    void deleteById(int id);

    void deleteByProposerIdOrProposeeId(int proposerId, int proposeeId);

    // id could be changed, so use the returned object
    MarriageRequest insert(MarriageRequest marriageRequest);

}
