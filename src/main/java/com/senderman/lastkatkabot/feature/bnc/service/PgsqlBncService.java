package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class PgsqlBncService implements BncService {

    private final BncRepository repo;

    public PgsqlBncService(BncRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<BncGameSave> findById(long id) {
        return repo.findById(id);
    }

    @Override
    public boolean existsById(long id) {
        return repo.existsById(id);
    }

    @Override
    public void deleteById(long id) {
        repo.deleteById(id);
    }

    @Override
    public BncGameSave save(BncGameSave bncGameSave) {
        return repo.existsById(bncGameSave.getId()) ? repo.update(bncGameSave) : repo.save(bncGameSave);
    }
}
