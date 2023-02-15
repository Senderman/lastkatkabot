package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.UserStats;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends CrudRepository<UserStats, Long> {

    Optional<UserStats> findByLoverId(int loverId);

    List<UserStats> findTop10ByOrderByBncScoreDesc();

    @MongoFindQuery(value = "{ _id: { $in: ?0 }, bncScore: { $ne: 0 } }", sort = "{ bncScore : -1 }")
    List<UserStats> findTop10ByOrderByBncScoreDescByUserIdIn(List<Long> ids);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends UserStats> S update(@Valid @NotNull S entity);
}
