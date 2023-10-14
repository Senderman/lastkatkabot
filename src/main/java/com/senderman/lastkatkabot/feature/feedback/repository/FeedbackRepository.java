package com.senderman.lastkatkabot.feature.feedback.repository;

import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.mongodb.annotation.MongoFindOptions;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {

    @MongoFindQuery(value = "{}", sort = "{ _id: -1 }")
    @MongoFindOptions(limit = 1)
    Optional<Feedback> findFirstOrderByIdDesc();

    long deleteByIdBetween(int from, int to);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends Feedback> S update(@NonNull S entity);
}
