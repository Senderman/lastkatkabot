package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Feedback;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@MongoRepository
public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {

    Optional<Feedback> findFirstOrderByIdDesc();

    long deleteByIdBetween(int from, int to);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends Feedback> S update(@Valid @NotNull S entity);
}
