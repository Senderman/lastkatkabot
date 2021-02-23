package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class LastPairs implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final ChatInfoService chats;

    public LastPairs(CommonAbsSender telegram, ChatInfoService chats) {
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
            Methods.sendMessage(chatId, "Команду нельзя использовать в ЛС!").callAsync(telegram);
            return;
        }

        var chatInfo = chats.findById(chatId);
        var pairs = chatInfo.getLastPairs();
        if (pairs == null || pairs.isEmpty()) {
            Methods.sendMessage(chatId, "В этом чате еще ни разу не запускали /pair!").callAsync(telegram);
            return;
        }

        var text = "<b>Последние 10 пар:</b>\n\n" + String.join("\n", pairs);
        Methods.sendMessage(chatId, text).callAsync(telegram);
    }
}
