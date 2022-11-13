package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;

@Command(
        command = "/bnchelp",
        description = "помощь по игре Быки и Коровы"
)
public class BncHelpCommand extends CommandExecutor {

    private final BotConfig config;

    public BncHelpCommand(BotConfig config) {
        this.config = config;
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.replyWithPhoto()
                .setFile(config.bncHelpPictureId())
                .callAsync(ctx.sender);
    }
}
