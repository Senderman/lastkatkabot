package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;

@Component
public class Cake implements CommandExecutor {

    public Cake() {
    }

    @Override
    public String getTrigger() {
        return "/cake";
    }

    @Override
    public String getDescription() {
        return "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней";
    }

    @Override
    public void execute(MessageContext ctx) {
        if (!ctx.message().isReply() || ctx.message().isUserMessage()) return;


        var subjectName = Html.htmlSafe(ctx.user().getFirstName());
        var target = ctx.message().getReplyToMessage().getFrom();
        var targetName = Html.htmlSafe(target.getFirstName());
        var text = String.format("\uD83C\uDF82 %s, пользователь %s подарил вам тортик %s",
                targetName, subjectName, ctx.message().getText().replaceAll("/@\\S*\\s?|/\\S*\\s?", ""));

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Принять")
                        .payload(Callbacks.CAKE + " accept " + target.getId()))
                .addButton(ButtonBuilder.callbackButton()
                        .text("Отказаться")
                        .payload(Callbacks.CAKE + " decline " + target.getId()))
                .build();

        ctx.reply(text)
                .setReplyToMessageId(ctx.message().getReplyToMessage().getMessageId())
                .setReplyMarkup(markup)
                .callAsync(ctx.sender);
    }

}
