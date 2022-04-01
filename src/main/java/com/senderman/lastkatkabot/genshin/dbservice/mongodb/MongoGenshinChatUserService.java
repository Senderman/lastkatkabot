package com.senderman.lastkatkabot.genshin.dbservice.mongodb;

import com.senderman.lastkatkabot.genshin.dbservice.GenshinChatUserService;
import com.senderman.lastkatkabot.genshin.model.GenshinChatUser;
import com.senderman.lastkatkabot.genshin.repository.GenshinChatUserRepository;
import org.springframework.stereotype.Service;

@Service
public class MongoGenshinChatUserService implements GenshinChatUserService {

    private final GenshinChatUserRepository repo;

    public MongoGenshinChatUserService(GenshinChatUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public GenshinChatUser findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId).orElseGet(() -> new GenshinChatUser(chatId, userId));
    }

    @Override
    public GenshinChatUser save(GenshinChatUser user) {
        return repo.save(user);
    }
}
