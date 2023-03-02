package com.senderman.lastkatkabot.feature.roleplay.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;

@Command
public class CakeCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/cake";
    }

    @Override
    public String getDescription() {
        return "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней";
    }

    @Override
    public void accept(MessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) return;


        var subjectName = Html.htmlSafe(ctx.user().getFirstName());
        var target = ctx.message().getReplyToMessage().getFrom();
        var targetName = Html.htmlSafe(target.getFirstName());
        var text = "\uD83C\uDF82 %s, пользователь %s подарил вам тортик %s".formatted(
                targetName, subjectName, ctx.message().getText().replaceAll("/@\\S*\\s?|/\\S*\\s?", ""));

        ctx.reply(text)
                .inReplyTo(ctx.message().getReplyToMessage())
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text("Принять")
                                .payload(CakeCallback.NAME, "accept", target.getId())
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text("Отказаться")
                                .payload(CakeCallback.NAME, "decline", target.getId())
                                .create()
                )
                .callAsync(ctx.sender);
    }

}
