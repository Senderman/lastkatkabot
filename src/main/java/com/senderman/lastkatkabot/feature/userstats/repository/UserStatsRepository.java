package com.senderman.lastkatkabot.feature.userstats.repository;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface UserStatsRepository extends CrudRepository<UserStats, Long> {

    @Query("SELECT * FROM USER_STATS WHERE bnc_score != 0 ORDER BY bnc_score DESC LIMIT 10")
    List<UserStats> findTop10OrderByBncScoreDesc();

    @Query("""
            SELECT s.* FROM USER_STATS s
            JOIN CHAT_USER u ON s.USER_ID = u.USER_ID
            WHERE u.CHAT_ID = :chatId
            AND s.BNC_SCORE != 0
            ORDER BY s.BNC_SCORE DESC
            LIMIT 10;
            """)
    List<UserStats> findTop10ByChatIdOrderByBncScoreDesc(long chatId);

    @Query("SELECT * FROM USER_STATS WHERE user_id IN (:ids) AND lover_id IN (:ids)")
    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

}
