package com.senderman.lastkatkabot.feature.userstats.repository;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import io.micronaut.data.mongodb.annotation.MongoFindOptions;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@MongoRepository
public interface UserStatsRepository extends CrudRepository<UserStats, Long> {

    Optional<UserStats> findByLoverId(int loverId);

    @MongoFindQuery(value = "{ bncScore: { $ne: 0 } }", sort = "{ bncScore : -1 }")
    @MongoFindOptions(limit = 10)
    List<UserStats> findTop10OrderByBncScoreDesc();

    @MongoFindQuery(value = "{ _id: { $in: :ids }, bncScore: { $ne: 0 } }", sort = "{ bncScore : -1 }")
    @MongoFindOptions(limit = 10)
    List<UserStats> findTop10ByUserIdInOrderByBncScoreDesc(List<Long> ids);

    @MongoFindQuery("{ _id: { $in: :ids }, loverId: { $in: :ids } }")
    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends UserStats> S update(@Valid @NotNull S entity);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends UserStats> Iterable<S> updateAll(@Valid @NotNull Iterable<S> entities);
}
