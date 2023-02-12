package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.model.BlacklistedChat;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Consumer;

@Singleton
public class BadChatCommand implements CommandExecutor {

    private final BlacklistedChatService database;
    private final Consumer<Long> chatPolicyViolationConsumer;

    public BadChatCommand(
            BlacklistedChatService blacklistedChatService,
            @Named("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer
    ) {
        this.database = blacklistedChatService;
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
    }

    @Override
    public String command() {
        return "/badchat";
    }

    @Override
    public String getDescription() {
        return "добавить чат в чс. /badchat <chatId>";
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
        chatPolicyViolationConsumer.accept(chatId);
        ctx.replyToMessage("✅ Чат успешно добавлен в чс!").callAsync(ctx.sender);
    }
}
