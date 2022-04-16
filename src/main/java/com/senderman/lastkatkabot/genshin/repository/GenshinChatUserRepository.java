package com.senderman.lastkatkabot.genshin.repository;

import com.senderman.lastkatkabot.genshin.model.GenshinChatUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenshinChatUserRepository extends CrudRepository<GenshinChatUser, String> {

    Optional<GenshinChatUser> findByChatIdAndUserId(long chatId, long userId);

}