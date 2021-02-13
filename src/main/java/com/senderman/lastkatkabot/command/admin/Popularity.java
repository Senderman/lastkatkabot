package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.ChatUser;
import com.senderman.lastkatkabot.repository.ChatUserRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;
import java.util.stream.StreamSupport;

@Component
public class Popularity implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final ChatUserRepository chatUsers;

    public Popularity(CommonAbsSender telegram, ChatUserRepository chatUsers) {
        this.telegram = telegram;
        this.chatUsers = chatUsers;
    }

    @Override
    public String getTrigger() {
        return "/popularity";
    }

    @Override
    public String getDescription() {
        return "популярность бота";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN, Role.ADMIN);
    }

    @Override
    public void execute(Message message) {
        var text = "\uD83D\uDCCA <b>Популярность бота:</b>\n\n";
        var chatsWithUsers = StreamSupport.stream(chatUsers.findAll().spliterator(), false)
                .map(ChatUser::getChatId)
                .distinct()
                .count();
        text += "\uD83D\uDC65 Активные чаты: " + chatsWithUsers + "\n\n";
        var users = StreamSupport.stream(chatUsers.findAll().spliterator(), false)
                .map(ChatUser::getUserId)
                .distinct()
                .count();
        text += "\uD83D\uDC64 Уникальные пользователи: " + users;
        Methods.sendMessage(message.getChatId(), text).callAsync(telegram);
    }
}
