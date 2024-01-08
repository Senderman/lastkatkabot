package com.senderman.lastkatkabot.feature.chatsettings.service;

import com.senderman.lastkatkabot.feature.chatsettings.model.ChatInfo;
import com.senderman.lastkatkabot.feature.chatsettings.repository.ChatInfoRepository;
import jakarta.inject.Singleton;

@Singleton
public class H2ChatInfoService implements ChatInfoService {

    private final ChatInfoRepository repo;

    public H2ChatInfoService(ChatInfoRepository repo) {
        this.repo = repo;
    }

    @Override
    public ChatInfo findById(long chatId) {
        return repo.findById(chatId).orElseGet(() -> new ChatInfo(chatId));
    }

    @Override
    public ChatInfo save(ChatInfo chatInfo) {
        return repo.existsById(chatInfo.getChatId()) ? repo.update(chatInfo) : repo.save(chatInfo);
    }
}
