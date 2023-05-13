package com.senderman.lastkatkabot.feature.members.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class GreetingCallback implements CallbackExecutor {

    public final static String NAME = "GREETING";

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(@NotNull L10nCallbackQueryContext context) {
        context.answer(context.getString("members.greeting.callback"))
                .setShowAlert(false)
                .callAsync(context.sender);
    }
}
