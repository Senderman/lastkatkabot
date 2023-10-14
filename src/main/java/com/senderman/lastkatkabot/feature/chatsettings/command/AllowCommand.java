package com.senderman.lastkatkabot.feature.chatsettings.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.chatsettings.service.CommandAccessManager;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

@Singleton // do not annotate with @Command to avoid cyclic dependencies
public class AllowCommand implements CommandExecutor {

    private final CommandAccessManager commandAccessManager;

    public AllowCommand(CommandAccessManager commandAccessManager) {
        this.commandAccessManager = commandAccessManager;
    }

    @Override
    public String command() {
        return "/callow";
    }

    @Override
    public String getDescription() {
        return "chatsettings.callow.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
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

        try {
            commandAccessManager.allowCommands(chatId, Arrays.asList(ctx.arguments()));
            ctx.replyToMessage(ctx.getString("chatsettings.callow.success")).callAsync(ctx.sender);
        } catch (CommandAccessManager.CommandsNotExistsException e) {
            ctx.replyToMessage(
                            ctx.getString("chatsettings.callow.failure")
                                    .formatted(String.join("\n", e.getCommands())))
                    .callAsync(ctx.sender);
        }
    }

}
