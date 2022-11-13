package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.model.BlacklistedChat;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.function.Consumer;

@Command(
        command = "/badchat",
        description = "добавить чат в чс. /badchat <chatId>",
        authority = {Role.ADMIN, Role.MAIN_ADMIN}
)
public class BadChatCommand extends CommandExecutor {

    private final BlacklistedChatService database;
    private final Consumer<Long> chatPolicyViolationConsumer;

    public BadChatCommand(
            BlacklistedChatService blacklistedChatService,
            @Qualifier("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer
    ) {
        this.database = blacklistedChatService;
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
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
