package com.senderman.lastkatkabot.config.service;

import com.senderman.lastkatkabot.config.model.Settings;
import com.senderman.lastkatkabot.config.repository.SettingsRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class MongoSettingsService implements SettingsService {

    private final SettingsRepository repo;

    public MongoSettingsService(SettingsRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Settings> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public void deleteById(String id) {
        repo.deleteById(id);
    }

    @Override
    public Settings save(Settings s) {
        return repo.update(s);
    }
}
