package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
@Command
public class BncHelpCommand implements CommandExecutor {

    private final BotConfig config;

    public BncHelpCommand(BotConfig config) {
        this.config = config;
    }

    @Override
    public String command() {
        return "/bnchelp";
    }

    @Override
    public String getDescription() {
        return "помощь по игре Быки и Коровы";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        ctx.replyWithPhoto()
                .setFile(config.bncHelpPictureId())
                .callAsync(ctx.sender);
    }
}
