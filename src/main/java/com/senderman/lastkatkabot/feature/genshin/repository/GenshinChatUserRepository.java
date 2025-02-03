package com.senderman.lastkatkabot.feature.genshin.repository;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinChatUser;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface GenshinChatUserRepository extends CrudRepository<GenshinChatUser, GenshinChatUser.PrimaryKey> {

    Optional<GenshinChatUser> findByChatIdAndUserId(long chatId, long userId);

    void deleteByUpdatedAtLessThan(LocalDateTime updatedAt);

}
