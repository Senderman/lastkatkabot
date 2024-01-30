package com.senderman.lastkatkabot.feature.userstats.repository;

import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    @Query("""
            SELECT s.* FROM USER_STATS s
            JOIN CHAT_USER u ON s.USER_ID = u.USER_ID
            WHERE u.CHAT_ID = :chatId;
            """)
    List<UserStats> findByChatId(long chatId);

    @Query("""
            SELECT s.* FROM USER_STATS s
            JOIN CHAT_USER u ON s.USER_ID = u.USER_ID
            WHERE u.CHAT_ID = :chatId
            ORDER BY RAND()
            LIMIT :amount
            """)
    List<UserStats> findRandomUsersOfChat(long chatId, int amount);

    @Query("""
            SELECT s.* FROM USER_STATS s
            JOIN CHAT_USER u ON s.USER_ID = u.USER_ID
            WHERE u.CHAT_ID = :chatId
            AND s.USER_ID = :userId;
            """)
    Optional<UserStats> findByChatIdAndUserId(long chatId, long userId);

    @Query("""
            UPDATE USER_STATS SET name = :name WHERE user_id = :userId;
            UPDATE USER_STATS SET locale = :locale WHERE user_id = :userId AND locale IS NULL;
            """)
    void updateByUserId(long userId, String name, String locale);

    void deleteByUpdatedAtLessThan(Timestamp updatedAt);

    @Query("""
            UPDATE USER_STATS
            SET LOVER_ID = NULL
            WHERE LOVER_ID NOT IN (SELECT USER_ID FROM USER_STATS)
            """)
    void updateNonExistentLovers();

}
