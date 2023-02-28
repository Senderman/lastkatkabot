package com.senderman.lastkatkabot.feature.genshin.repository;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinChatUser;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@MongoRepository
public interface GenshinChatUserRepository extends CrudRepository<GenshinChatUser, String> {

    Optional<GenshinChatUser> findByChatIdAndUserId(long chatId, long userId);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends GenshinChatUser> S update(@Valid @NotNull S entity);
}
