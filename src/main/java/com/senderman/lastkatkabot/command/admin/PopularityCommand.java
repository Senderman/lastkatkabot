package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import jakarta.inject.Singleton;

import java.util.EnumSet;

@Command
@Singleton
public class PopularityCommand implements CommandExecutor {

    private final ChatUserService chatUsers;

    public PopularityCommand(ChatUserService chatUsers) {
        this.chatUsers = chatUsers;
    }

    @Override
    public String command() {
        return "/popularity";
    }

    @Override
    public String getDescription() {
        return "популярность бота";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        var text = "📊 <b>Популярность бота:</b>\n\n";
        var chatsWithUsers = chatUsers.getTotalChats();
        text += "👥 Активные чаты: " + chatsWithUsers + "\n\n";
        var users = chatUsers.getTotalUsers();
        text += "👤 Уникальные пользователи: " + users;
        ctx.reply(text).callAsync(ctx.sender);
    }
}
