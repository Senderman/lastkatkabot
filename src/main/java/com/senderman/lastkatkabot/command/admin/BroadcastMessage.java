package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class BroadcastMessage implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final ChatUserRepository chatUsers;
    private final ExecutorService threadPool;

    public BroadcastMessage(CommonAbsSender telegram, ChatUserRepository chatUsers, ExecutorService threadPool) {
        this.telegram = telegram;
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
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
            ApiRequests.answerMessage(message, "Неверное количество аргументов!").call(telegram);
            return;
        }
        var messageToBroadcast = "\uD83D\uDD14 <b>Сообщение от разработчиков</b>\n\n" +
                message.getText().split("\\s+", 2)[1];
        Methods.sendMessage(chatId, "Начало рассылки сообщений...").call(telegram);
        threadPool.execute(() -> {
            int counter = 0;
            int totalCounter = 0;
            var chatIds = StreamSupport.stream(chatUsers.findAll().spliterator(), false)
                    .map(ChatUser::getChatId)
                    .distinct()
                    .collect(Collectors.toList());
            for (var chat : chatIds) {
                try {
                    var m = new SendMessage(Long.toString(chat), messageToBroadcast);
                    m.enableHtml(true);
                    telegram.execute(m);
                    counter++;
                } catch (TelegramApiException ignored) {
                } finally {
                    totalCounter++;
                }
            }
            ApiRequests.answerMessage(message, "Сообщение получили " + counter + "/" + totalCounter + " чатов")
                    .callAsync(telegram);
        });
    }
}
