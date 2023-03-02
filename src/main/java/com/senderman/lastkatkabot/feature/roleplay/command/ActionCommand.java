package com.senderman.lastkatkabot.feature.roleplay.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;

@Command
public class ActionCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/action";
    }

    @Override
    public String getDescription() {
        return "сделать действие. Действие указывать через пробел, можно реплаем";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        ctx.deleteMessage().callAsync(ctx.sender);
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) return;

        var action = ctx.user().getFirstName() + " " + ctx.argument(0);
        var sm = Methods.sendMessage(ctx.chatId(), action);
        if (ctx.message().isReply()) {
            sm.inReplyTo(ctx.message().getReplyToMessage());
        }
        sm.callAsync(ctx.sender);
    }
}