package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.commands.CommandRegistry;
import com.annimon.tgbotsmodule.commands.authority.Authority;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.callback.CallbackExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Singleton
public class CommandUpdateHandler extends CommandRegistry<Role> {

    public CommandUpdateHandler(
            @NotNull BotHandler handler,
            @NotNull Authority<Role> authority,
            Set<CommandExecutor> commands,
            Set<CallbackExecutor> callbacks,
            BncTelegramHandler bncTelegramHandler
    ) {
        super(handler, authority);

        splitCallbackCommandByWhitespace();

        register(bncTelegramHandler);
        commands.forEach(this::register);
        callbacks.forEach(this::register);
    }
}
