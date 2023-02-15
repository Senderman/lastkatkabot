package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatInfo;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    long deleteByChatIdIn(Collection<Long> chatIds);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends ChatInfo> S update(@Valid @NotNull S entity);
}
