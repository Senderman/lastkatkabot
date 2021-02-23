package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import org.springframework.stereotype.Service;

@Service
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
        return repository.save(chatInfo);
    }
}
