package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import org.springframework.stereotype.Component;

@Component
public class BncHelp implements CommandExecutor {

    private final String bncPhotoId;

    public BncHelp(BotConfig config) {
        this.bncPhotoId = config.bncHelpPictureId();
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
    public void accept(MessageContext ctx) {
        ctx.replyWithPhoto()
                .setFile(bncPhotoId)
                .callAsync(ctx.sender);
    }
}
