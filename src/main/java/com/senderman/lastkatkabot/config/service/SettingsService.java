package com.senderman.lastkatkabot.config.service;

import com.senderman.lastkatkabot.config.model.Settings;

import java.util.Optional;

public interface SettingsService {

    Optional<Settings> findById(String id);

    void deleteById(String id);

    Settings save(Settings s);

}
