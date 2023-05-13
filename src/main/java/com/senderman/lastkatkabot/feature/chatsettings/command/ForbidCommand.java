package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.CommandAccessManager;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class ForbidCommand implements CommandExecutor {

    private final CommandAccessManager commandAccessManager;

    public ForbidCommand(CommandAccessManager commandAccessManager) {
        this.commandAccessManager = commandAccessManager;
    }

    @Override
    public String command() {
        return "/cforbid";
    }

    @Override
    public String getDescription() {
        return "chatsettings.cforbid.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var chatMember = Methods.getChatMember(chatId, userId).call(ctx.sender);
        if (chatMember == null || !Set.of("administrator", "creator").contains(chatMember.getStatus())) {
            ctx.replyToMessage(ctx.getString("common.mustBeChatAdmin")).callAsync(ctx.sender);
            return;
        }
        if (ctx.argumentsLength() == 0) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        var commandList = Arrays.asList(ctx.arguments());
        if (commandList.contains("/callow")) {
            ctx.replyToMessage(ctx.getString("chatsettings.cforbid.wrongUsage")).callAsync(ctx.sender);
            return;
        }

        try {
            commandAccessManager.forbidCommands(chatId, commandList);
            ctx.replyToMessage(ctx.getString("chatsettings.cforbid.success")).callAsync(ctx.sender);
        } catch (CommandAccessManager.CommandsNotExistsException e) {
            ctx.replyToMessage(
                            ctx.getString("chatsettings.cforbid.failure")
                                    .formatted(String.join("\n", e.getCommands())))
                    .callAsync(ctx.sender);
        }
    }
}
