package com.senderman.lastkatkabot.command.admin;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.ChatInfoRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.EnumSet;

@Component
public class BroadcastMessage implements CommandExecutor {

    private final ApiRequests telegram;
    private final ChatInfoRepository chats;

    public BroadcastMessage(ApiRequests telegram, ChatInfoRepository chats) {
        this.telegram = telegram;
        this.chats = chats;
    }

    @Override
    public String getTrigger() {
        return "/broadcast";
    }

    @Override
    public String getDescription() {
        return "разослать всем сообщение";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        if (message.getText().strip().equals(getTrigger())) {
            telegram.sendMessage(chatId, "Неверное количество аргументов!", message.getMessageId());
            return;
        }
        var messageToBroadcast = message.getText().split("\\s+", 2)[1];
        telegram.sendMessage(chatId, "Начало рассылки сообщений...");
        new Thread(() -> {
            int counter = 0;
            int totalCounter = 0;
            for (var chat : chats.findAll()) {
                try {
                    var m = new SendMessage(Long.toString(chat.getChatId()), messageToBroadcast);
                    m.enableHtml(true);
                    telegram.tryExecute(m);
                    counter++;
                } catch (TelegramApiException ignored) {
                } finally {
                    totalCounter++;
                }
            }
            telegram.sendMessage(chatId,
                    "Сообщение получили " + counter + "/" + totalCounter + " чатов",
                    message.getMessageId());
        }).start();
    }
}
