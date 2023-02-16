package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedChat;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@MongoRepository
public interface BlacklistedChatRepository extends CrudRepository<BlacklistedChat, Long> {

    List<BlacklistedChat> findByChatIdIn(Collection<Long> ids);

}
