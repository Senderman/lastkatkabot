package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BncHelp implements CommandExecutor {

    private final String bncPhotoId;

    public BncHelp(@Value("${bnc-help-picture-id}") String bncPhotoId) {
        this.bncPhotoId = bncPhotoId;
    }

    @Override
    public String getTrigger() {
        return "/bnchelp";
    }

    @Override
    public String getDescription() {
        return "помощь по игре Быки и Коровы";
    }

    @Override
    public void execute(MessageContext ctx) {
        ctx.replyWithPhoto()
                .setFile(bncPhotoId)
                .callAsync(ctx.sender);
    }
}
