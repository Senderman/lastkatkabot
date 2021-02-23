package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BncGameSave;

import java.util.Optional;

public interface BncService {

    Optional<BncGameSave> findById(long id);

    boolean existsById(long id);

    void deleteById(long id);

    BncGameSave save(BncGameSave bncGameSave);

}
