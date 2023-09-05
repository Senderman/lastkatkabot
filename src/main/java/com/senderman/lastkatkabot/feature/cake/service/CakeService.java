package com.senderman.lastkatkabot.feature.cake.service;

import com.senderman.lastkatkabot.feature.cake.model.Cake;

import java.util.Optional;

public interface CakeService {

    Optional<Cake> findById(int id);

    void deleteById(int id);

    long count();

    /**
     * Insert new cake into database. Note that the id can be changed, so use the returned object
     *
     * @param cake cake to insert
     * @return the actually saved object (id can be different)
     */
    Cake insert(Cake cake);

    Optional<Cake> findFirstOrderByIdDesc();

}
