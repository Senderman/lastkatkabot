package com.senderman.lastkatkabot.feature.genshin.service;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinChatUser;
import com.senderman.lastkatkabot.feature.genshin.repository.GenshinChatUserRepository;
import jakarta.inject.Singleton;


@Singleton
public class PgsqlGenshinChatUserService implements GenshinChatUserService {

    private final GenshinChatUserRepository repo;

    public PgsqlGenshinChatUserService(GenshinChatUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public GenshinChatUser findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId).orElseGet(() -> new GenshinChatUser(chatId, userId));
    }

    @Override
    public GenshinChatUser save(GenshinChatUser user) {
        return repo.existsById(user.getPrimaryKey()) ? repo.update(user) : repo.save(user);
    }
}
