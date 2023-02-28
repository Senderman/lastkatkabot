package com.senderman.lastkatkabot.feature.chatsettings.service;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import jakarta.inject.Singleton;


@Singleton
public class MongoChatInfoService implements ChatInfoService {

    private final ChatInfoRepository repository;

    public MongoChatInfoService(ChatInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChatInfo findById(long chatId) {
        return repository.findById(chatId).orElseGet(() -> new ChatInfo(chatId));
    }

    @Override
    public ChatInfo save(ChatInfo chatInfo) {
        return repository.update(chatInfo);
    }
}
