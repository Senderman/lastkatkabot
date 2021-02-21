package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.MarriageRequestService;
import com.senderman.lastkatkabot.model.MarriageRequest;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MongoMarriageRequestService implements MarriageRequestService {

    private final MarriageRequestRepository repository;

    public MongoMarriageRequestService(MarriageRequestRepository repository) {
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
    public void deleteByProposerIdOrProposeeId(int proposerId, int proposeeId) {
        repository.deleteByProposerIdOrProposeeId(proposerId, proposeeId);
    }

    @Override
    public MarriageRequest insert(MarriageRequest marriageRequest) {
        int id = repository.findFirstByOrderByIdDesc().map(r -> r.getId() + 1).orElse(1);
        marriageRequest.setId(id);
        return repository.save(marriageRequest);
    }
}
