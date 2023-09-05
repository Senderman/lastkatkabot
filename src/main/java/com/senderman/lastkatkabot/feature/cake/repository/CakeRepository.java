package com.senderman.lastkatkabot.feature.cake.repository;

import com.senderman.lastkatkabot.feature.cake.model.Cake;
import io.micronaut.data.mongodb.annotation.MongoFindOptions;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface CakeRepository extends CrudRepository<Cake, Integer> {

    @MongoFindQuery(value = "{}", sort = "{ _id: -1 }")
    @MongoFindOptions(limit = 1)
    Optional<Cake> findFirstOrderByIdDesc();

    long deleteByCreatedAtLessThan(int createdAt);

}
