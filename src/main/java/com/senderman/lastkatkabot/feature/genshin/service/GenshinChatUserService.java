package com.senderman.lastkatkabot.feature.genshin.service;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinChatUser;

public interface GenshinChatUserService {

    GenshinChatUser findByChatIdAndUserId(long chatId, long userId);

    GenshinChatUser save(GenshinChatUser user);

}
