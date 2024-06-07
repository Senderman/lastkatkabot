package com.senderman.lastkatkabot.feature.userstats.repository;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface UserStatsRepository extends CrudRepository<UserStats, Long> {

    @Query("SELECT * FROM user_stats WHERE bnc_score != 0 ORDER BY bnc_score DESC LIMIT 10")
    List<UserStats> findTop10OrderByBncScoreDesc();

    @Query("""
            SELECT s.* FROM user_stats s
            JOIN chat_user u ON s.user_id = u.user_id
            WHERE u.chat_id = :chatId
            AND s.bnc_score != 0
            ORDER BY s.bnc_score DESC
            LIMIT 10;
            """)
    List<UserStats> findTop10ByChatIdOrderByBncScoreDesc(long chatId);

    @Query("SELECT * FROM user_stats WHERE user_id IN (:ids) AND lover_id IN (:ids)")
    List<UserStats> findByIdAndLoverIdIn(Collection<Long> ids);

    @Query("""
            SELECT s.* FROM user_stats s
            JOIN chat_user u ON s.user_id = u.user_id
            WHERE u.chat_id = :chatId;
            """)
    List<UserStats> findByChatId(long chatId);

    @Query("""
            SELECT s.* FROM user_stats s
            JOIN chat_user u ON s.user_id = u.user_id
            WHERE u.chat_id = :chatId
            ORDER BY RANDOM()
            LIMIT :amount
            """)
    List<UserStats> findRandomUsersOfChat(long chatId, int amount);

    @Query("""
            SELECT s.* FROM user_stats s
            JOIN chat_user u ON s.user_id = u.user_id
            WHERE u.chat_id = :chatId
            AND s.user_id = :userId;
            """)
    Optional<UserStats> findByChatIdAndUserId(long chatId, long userId);

    @Query("""
            UPDATE user_stats SET name = :name WHERE user_id = :userId;
            UPDATE user_stats SET locale = :locale WHERE user_id = :userId AND locale IS NULL;
            """)
    void updateByUserId(long userId, String name, @Nullable String locale);

    void deleteByUpdatedAtLessThan(Timestamp updatedAt);

    @Query("""
            UPDATE user_stats
            SET lover_id = NULL
            WHERE lover_id NOT IN (SELECT user_id FROM user_stats)
            """)
    void updateNonExistentLovers();

}
