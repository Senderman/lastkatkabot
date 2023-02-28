package com.senderman.lastkatkabot.feature.members.command;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.command.Callbacks;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class GreetingCallback implements CallbackExecutor {

    @Override
    public String command() {
        return Callbacks.GREETING;
    }

    @Override
    public void accept(@NotNull CallbackQueryContext context) {
        context.answer("Это приветствие пользователя!")
                .setShowAlert(false)
                .callAsync(context.sender);
    }
}
