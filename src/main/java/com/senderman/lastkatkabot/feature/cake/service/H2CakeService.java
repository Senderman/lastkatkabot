package com.senderman.lastkatkabot.feature.cake.service;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import com.senderman.lastkatkabot.feature.cake.repository.CakeRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class H2CakeService implements CakeService {

    private final CakeRepository repo;

    public H2CakeService(CakeRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Cake> findById(int id) {
        return repo.findById(id);
    }

    @Override
    public void deleteById(int id) {
        repo.deleteById(id);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public Cake insert(Cake cake) {
        return repo.save(cake);
    }

    @Override
    public int getLowestAvailableId() {
        return repo.getLowestAvailableId().orElse(1);
    }

}
