package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;

@Component
public class Popularity implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final ChatUserService chatUsers;

    public Popularity(CommonAbsSender telegram, ChatUserService chatUsers) {
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
        var chatsWithUsers = chatUsers.getTotalChats();
        text += "\uD83D\uDC65 Активные чаты: " + chatsWithUsers + "\n\n";
        var users = chatUsers.getTotalUsers();
        text += "\uD83D\uDC64 Уникальные пользователи: " + users;
        Methods.sendMessage(message.getChatId(), text).callAsync(telegram);
    }
}
