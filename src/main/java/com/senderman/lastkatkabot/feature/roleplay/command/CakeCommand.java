package com.senderman.lastkatkabot.feature.roleplay.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

@Command
public class CakeCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/cake";
    }

    @Override
    public String getDescription() {
        return "roleplay.cake.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) return;


        var subjectName = Html.htmlSafe(ctx.user().getFirstName());
        var target = ctx.message().getReplyToMessage().getFrom();
        var targetName = Html.htmlSafe(target.getFirstName());
        var text = ctx.getString("roleplay.cake.message").formatted(
                targetName, subjectName, ctx.message().getText().replaceAll("/@\\S*\\s?|/\\S*\\s?", ""));

        ctx.reply(text)
                .inReplyTo(ctx.message().getReplyToMessage())
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("roleplay.cake.acceptButton"))
                                .payload(CakeCallback.NAME, "accept", target.getId())
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("roleplay.cake.declineButton"))
                                .payload(CakeCallback.NAME, "decline", target.getId())
                                .create()
                )
                .callAsync(ctx.sender);
    }

}
