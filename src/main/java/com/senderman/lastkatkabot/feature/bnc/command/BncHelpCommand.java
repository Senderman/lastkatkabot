package com.senderman.lastkatkabot.feature.bnc.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.media.Media;
import com.senderman.lastkatkabot.feature.media.MediaIdService;
import org.jetbrains.annotations.NotNull;

@Command
public class BncHelpCommand implements CommandExecutor {

    private final MediaIdService mediaIdService;

    public BncHelpCommand(MediaIdService mediaIdService) {
        this.mediaIdService = mediaIdService;
    }

    @Override
    public String command() {
        return "/bnchelp";
    }

    @Override
    public String getDescriptionKey() {
        return "bnc.bnchelp.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var method = ctx.replyWithPhoto();
        mediaIdService.setMedia(method, Media.BNCHELP);
        method
                .callAsync(ctx.sender)
                .thenAccept(m -> mediaIdService.setFileId(Media.BNCHELP, m.getPhoto().getFirst().getFileId()));
    }
}
