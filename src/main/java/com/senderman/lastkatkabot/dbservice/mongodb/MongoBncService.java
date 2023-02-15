package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.BncService;
import com.senderman.lastkatkabot.model.BncGameSave;
import com.senderman.lastkatkabot.repository.BncRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class MongoBncService implements BncService {

    private final BncRepository repository;

    public MongoBncService(BncRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BncGameSave> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    @Override
    public BncGameSave save(BncGameSave bncGameSave) {
        return repository.update(bncGameSave);
    }
}
