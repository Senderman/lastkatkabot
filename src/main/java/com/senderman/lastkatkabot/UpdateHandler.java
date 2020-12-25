package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@SpringBootApplication
public class UpdateHandler extends BotHandler {

    private final String username;
    private final String token;
    private final HandlerExtractor<CommandExecutor> commands;
    private final HandlerExtractor<CallbackExecutor> callbacks;

    @Autowired
    public UpdateHandler(@Value("${login}") String login,
                         @Lazy HandlerExtractor<CommandExecutor> commandExtractor,
                         @Lazy HandlerExtractor<CallbackExecutor> callbacks) {
        var args = login.split("\\s+");
        username = args[0];
        token = args[1];
        this.commands = commandExtractor;
        this.callbacks = callbacks;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updates
                .stream()
                .filter(this::filterUpdate)
                .collect(Collectors.toList())
                .forEach(this::onUpdateReceived);
    }

    @Override
    protected BotApiMethod onUpdate(@NotNull Update update) {

        if (!update.hasMessage()) return null;

        var message = update.getMessage();

        if (!message.hasText()) return null;

        var text = message.getText();

        if (!message.isCommand()) return null;

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */
        var command = text.split("\\s+", 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@" + getBotUsername(), "");
        if (command.contains("@")) return null;

        var executor = commands.findExecutor(command);
        if (executor == null) return null;
        executor.execute(message);

        return null;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    // this method contains filtering rules for all updates
    private boolean filterUpdate(Update update) {
        if (update.hasCallbackQuery()) return true;
        var message = update.getMessage();
        if (message == null) return false;

        // Skip messages older than 2 minutes
        if (message.getDate() + 120 < System.currentTimeMillis() / 1000) return false;

        return true;
    }
}
