package com.senderman.lastkatkabot.feature.chatsettings.service;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;

public interface ChatInfoService {

    ChatInfo findById(long chatId);

    ChatInfo save(ChatInfo chatInfo);

}
