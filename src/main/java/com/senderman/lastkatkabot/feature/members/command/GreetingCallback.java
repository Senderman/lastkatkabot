package com.senderman.lastkatkabot.feature.members.command;

import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedCallbackQueryContext;
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
    public void accept(@NotNull LocalizedCallbackQueryContext context) {
        context.answer(context.getString("members.greeting.callback"))
                .setShowAlert(false)
                .callAsync(context.sender);
    }
}
