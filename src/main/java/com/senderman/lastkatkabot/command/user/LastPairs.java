package com.senderman.lastkatkabot.command.user;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.ChatInfo;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class LastPairs implements CommandExecutor {

    private final ApiRequests telegram;
    private final ChatInfoRepository chats;

    public LastPairs(ApiRequests telegram, ChatInfoRepository chats) {
        this.telegram = telegram;
        this.chats = chats;
    }

    @Override
    public String getTrigger() {
        return "/lastpairs";
    }

    @Override
    public String getDescription() {
        return "последние 10 пар чата";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        if (message.isUserMessage()) {
            telegram.sendMessage(chatId, "Команду нельзя использовать в ЛС!");
            return;
        }

        var chatInfo = chats.findById(chatId).orElseGet(() -> new ChatInfo(chatId));
        var pairs = chatInfo.getLastPairs();
        if (pairs == null || pairs.isEmpty()) {
            telegram.sendMessage(chatId, "В этом чате еще ни разу не запускали /pair!");
            return;
        }

        var text = "<b>Последние 10 пар:</b>\n\n" + String.join("\n", pairs);
        telegram.sendMessage(chatId, text);
    }
}
