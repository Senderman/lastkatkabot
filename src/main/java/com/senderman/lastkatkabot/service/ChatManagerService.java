package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatManagerService {

    private final ChatInfoRepository chats;
    private final ChatUserRepository chatUsers;
    private final Set<Long> runningChatMigrations = Collections.synchronizedSet(new HashSet<>());

    public ChatManagerService(ChatInfoRepository chats, ChatUserRepository chatUsers) {
        this.chats = chats;
        this.chatUsers = chatUsers;
    }

    public boolean migrateChatIfNeeded(long oldChatId, long newChatId) {
        if (runningChatMigrations.contains(oldChatId)) return false;

        runningChatMigrations.add(oldChatId);
        new Thread(() -> {
            var chat = chats.findById(oldChatId).orElse(new ChatInfo());
            chat.setChatId(newChatId);
            chats.save(chat);

            var updatedUsers = chatUsers.findAllByChatId(oldChatId)
                    .parallel()
                    .peek(user -> user.setChatId(newChatId))
                    .collect(Collectors.toList());
            chatUsers.saveAll(updatedUsers);
            runningChatMigrations.remove(oldChatId);
        }).start();

        return true;
    }


}
