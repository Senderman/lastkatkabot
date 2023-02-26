package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

@Singleton
@Command
public class PopularityCommand implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public PopularityCommand(ChatUserService chatUsers, @Named("generalNeedsPool") ExecutorService threadPool) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
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
        threadPool.execute(() -> {
            var text = "📊 <b>Популярность бота:</b>\n\n";
            var chatsWithUsers = chatUsers.getTotalChats();
            text += "👥 Активные чаты: " + chatsWithUsers + "\n\n";
            var users = chatUsers.getTotalUsers();
            text += "👤 Уникальные пользователи: " + users;
            ctx.reply(text).callAsync(ctx.sender);
        });
    }
}
