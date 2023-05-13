package com.senderman.lastkatkabot.feature.access.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.access.model.BlacklistedChat;
import com.senderman.lastkatkabot.feature.access.service.BlacklistedChatService;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Command
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
        return "access.badchat.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        if (ctx.argumentsLength() == 0) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        long chatId;
        try {
            chatId = Long.parseLong(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("common.chatIdIsNumber")).callAsync(ctx.sender);
            return;
        }

        database.save(new BlacklistedChat(chatId));
        Methods.sendMessage(chatId, ctx.getString("common.yourChatIsBad")).callAsync(ctx.sender);
        Methods.leaveChat(chatId).callAsync(ctx.sender);
        ctx.replyToMessage(ctx.getString("access.badchat.success")).callAsync(ctx.sender);
    }
}
