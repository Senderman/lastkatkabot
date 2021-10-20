package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.model.BlacklistedChat;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class BadChatCommand implements CommandExecutor {

    private final BlacklistedChatService database;

    public BadChatCommand(BlacklistedChatService blacklistedChatService) {
        this.database = blacklistedChatService;
    }

    @Override
    public String command() {
        return "/badchat";
    }

    @Override
    public String getDescription() {
        return "добавить чат в чс. " + command() + " <chatId>";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        if (ctx.argumentsLength() == 0) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }

        long chatId;
        try {
            chatId = Long.parseLong(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage("ChatId - это число!").callAsync(ctx.sender);
            return;
        }

        database.save(new BlacklistedChat(chatId));
        ctx.replyToMessage("✅ Чат успешно добавлен в чс!").callAsync(ctx.sender);
    }
}
