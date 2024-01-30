package com.senderman.lastkatkabot.util.callback;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class NoOpCallback implements CallbackExecutor {

    public static String NAME = "NO_OP";

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(@NotNull L10nCallbackQueryContext ctx) {
        ctx.answer("").callAsync(ctx.sender);
    }
}
