package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.UserStats;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@MongoRepository
public interface UserStatsRepository extends CrudRepository<UserStats, Long> {

    Optional<UserStats> findByLoverId(int loverId);

    @MongoFindQuery(value = "{ bncScore: { $ne: 0 } }", sort = "{ bncScore : -1 }")
    List<UserStats> findTop10OrderByBncScoreDesc();

    @MongoFindQuery(value = "{ _id: { $in: :ids }, bncScore: { $ne: 0 } }", sort = "{ bncScore : -1 }")
    List<UserStats> findTop10ByUserIdInOrderByBncScoreDesc(List<Long> ids);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends UserStats> S update(@Valid @NotNull S entity);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends UserStats> Iterable<S> updateAll(@Valid @NotNull Iterable<S> entities);
}
