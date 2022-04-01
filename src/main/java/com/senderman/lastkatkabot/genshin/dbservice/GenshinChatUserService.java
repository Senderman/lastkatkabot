package com.senderman.lastkatkabot.genshin.dbservice;

import com.senderman.lastkatkabot.genshin.model.GenshinChatUser;

public interface GenshinChatUserService {

    GenshinChatUser findByChatIdAndUserId(long chatId, long userId);

    GenshinChatUser save(GenshinChatUser user);

}
