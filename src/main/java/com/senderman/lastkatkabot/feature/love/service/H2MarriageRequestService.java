package com.senderman.lastkatkabot.feature.love.service;

import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;
import com.senderman.lastkatkabot.feature.love.repository.MarriageRequestRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class H2MarriageRequestService implements MarriageRequestService {

    private final MarriageRequestRepository repository;

    public H2MarriageRequestService(MarriageRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MarriageRequest> findById(int id) {
        return repository.findById(id);
    }

    @Override
    public void delete(MarriageRequest marriageRequest) {
        repository.delete(marriageRequest);
    }

    @Override
    public void deleteById(int id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByProposerIdOrProposeeId(long proposerId, long proposeeId) {
        repository.deleteByProposerIdOrProposeeId(proposerId, proposeeId);
    }

    @Override
    public MarriageRequest insert(MarriageRequest marriageRequest) {
        return repository.save(marriageRequest);
    }

    @Override
    public int getLowestAvailableId() {
        return repository.getLowestAvailableId().orElse(1);
    }
}
