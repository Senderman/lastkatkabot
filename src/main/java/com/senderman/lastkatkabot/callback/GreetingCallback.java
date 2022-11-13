package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.annotation.Callback;
import org.jetbrains.annotations.NotNull;

@Callback(Callbacks.GREETING)
public class GreetingCallback extends CallbackExecutor {

    @Override
    public void accept(@NotNull CallbackQueryContext context) {
        context.answer("Это приветствие пользователя!")
                .setShowAlert(false)
                .callAsync(context.sender);
    }
}
