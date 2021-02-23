package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.ChatInfo;

public interface ChatInfoService {

    ChatInfo findById(long chatId);

    ChatInfo save(ChatInfo chatInfo);

}
