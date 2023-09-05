package com.senderman.lastkatkabot.feature.cake.service;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import com.senderman.lastkatkabot.feature.cake.repository.CakeRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class MongoCakeService implements CakeService {

    private final CakeRepository repo;

    public MongoCakeService(CakeRepository repo) {
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
        int id = repo.findFirstOrderByIdDesc().map(c -> c.getId() + 1).orElse(1);
        cake.setId(id);
        return repo.save(cake);
    }

    @Override
    public Optional<Cake> findFirstOrderByIdDesc() {
        return repo.findFirstOrderByIdDesc();
    }
}
