package com.senderman.lastkatkabot.feature.http.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class HttpCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/http";
    }

    @Override
    public String getDescription() {
        return "http.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        String arg = ctx.argument(0);
        if (!arg.matches("\\d{3}")) {
            ctx.replyToMessage(ctx.getString("http.isNumber")).callAsync(ctx.sender);
            return;
        }

        String message = "%s<a href=\"https://http.cat/images/%s.jpg\">\u200B</a>".formatted(arg, arg);
        var m = ctx.replyToMessage(message).call(ctx.sender);
        // since there's a method preprocessor that disables webPagePreview on SendMessage method,
        // we use EditMessage to re-enable it
        Methods.editMessageText(m.getChatId(), m.getMessageId(), message)
                .enableWebPagePreview()
                .callAsync(ctx.sender);
    }
}
