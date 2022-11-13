package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;

@Command(
        command = "/popularity",
        description = "популярность бота",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class PopularityCommand extends CommandExecutor {

    private final ChatUserService chatUsers;

    public PopularityCommand(ChatUserService chatUsers) {
        this.chatUsers = chatUsers;
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
