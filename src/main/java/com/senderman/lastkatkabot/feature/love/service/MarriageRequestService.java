package com.senderman.lastkatkabot.feature.love.service;

import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;

import java.util.Optional;

public interface MarriageRequestService {

    Optional<MarriageRequest> findById(int id);

    void delete(MarriageRequest marriageRequest);

    void deleteById(int id);

    void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId);

    // id could be changed, so use the returned object
    MarriageRequest insert(MarriageRequest marriageRequest);

    int getLowestAvailableId();

}
