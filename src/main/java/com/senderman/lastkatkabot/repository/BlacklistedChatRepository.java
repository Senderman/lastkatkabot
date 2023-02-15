package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BlacklistedChat;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BlacklistedChatRepository extends CrudRepository<BlacklistedChat, Long> {

    List<BlacklistedChat> findByChatIdIn(Collection<Long> ids);

}
