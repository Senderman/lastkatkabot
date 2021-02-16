package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BotHandlerExtension extends BotHandler {

    private final Map<String, Consumer<BotApiMethod<?>>> preprocessors = new HashMap<>();

    public BotHandlerExtension() {
        super(new FixedDefaultBotOptions());
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        preprocessMethod(method);
        return super.execute(method);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void executeAsync(Method method, Callback callback) throws TelegramApiException {
        preprocessMethod(method);
        super.executeAsync(method, callback);
    }

    protected void addMethodPreprocessor(String methodPath, Consumer<BotApiMethod<?>> preprocessor) {
        preprocessors.put(methodPath, preprocessor);
    }

    private <T extends Serializable, Method extends BotApiMethod<T>> void preprocessMethod(@NotNull Method method) {
        var preprocessor = preprocessors.get(method.getMethod());
        if (preprocessor == null) return;
        preprocessor.accept(method);
    }
}
