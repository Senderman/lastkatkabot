package com.senderman.lastkatkabot.feature.cake.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.cake.model.Cake;
import com.senderman.lastkatkabot.feature.cake.service.CakeService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

@Command
public class CakeCommand implements CommandExecutor {

    private final CakeService cakeService;

    public CakeCommand(CakeService cakeService) {
        this.cakeService = cakeService;
    }

    @Override
    public String command() {
        return "/cake";
    }

    @Override
    public String getDescription() {
        return "roleplay.cake.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage())
            return;
        ctx.setArgumentsLimit(1);

        var cake = cakeService.insert(new Cake(ctx.argument(0)));
        var subjectName = Html.htmlSafe(ctx.user().getFirstName());
        var target = ctx.message().getReplyToMessage().getFrom();
        var targetName = Html.htmlSafe(target.getFirstName());
        var text = ctx.getString("roleplay.cake.message").formatted(targetName, subjectName, cake.getFilling());

        ctx.reply(text)
                .inReplyTo(ctx.message().getReplyToMessage())
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("roleplay.cake.acceptButton"))
                                .payload(CakeCallback.NAME, "accept", target.getId(), cake.getId())
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("roleplay.cake.declineButton"))
                                .payload(CakeCallback.NAME, "decline", target.getId(), cake.getId())
                                .create()
                )
                .callAsync(ctx.sender);
    }

}
