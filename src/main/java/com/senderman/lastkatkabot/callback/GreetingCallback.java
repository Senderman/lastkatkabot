package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
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
