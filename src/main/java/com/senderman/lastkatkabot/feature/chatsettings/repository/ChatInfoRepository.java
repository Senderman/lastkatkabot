package com.senderman.lastkatkabot.feature.chatsettings.repository;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@MongoRepository
public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    long deleteByChatIdIn(Collection<Long> chatIds);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends ChatInfo> S update(@Valid @NotNull S entity);
}
