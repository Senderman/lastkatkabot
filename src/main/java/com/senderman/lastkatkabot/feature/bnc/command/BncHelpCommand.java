package com.senderman.lastkatkabot.feature.bnc.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import org.jetbrains.annotations.NotNull;

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
        return "bnc.bnchelp.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        ctx.replyWithPhoto()
                .setFile(config.getBncHelpPictureId())
                .callAsync(ctx.sender);
    }
}
