package com.senderman.lastkatkabot.feature.members.command;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.command.CallbackExecutor;
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
    public void accept(@NotNull CallbackQueryContext context) {
        context.answer("Это приветствие пользователя!")
                .setShowAlert(false)
                .callAsync(context.sender);
    }
}
