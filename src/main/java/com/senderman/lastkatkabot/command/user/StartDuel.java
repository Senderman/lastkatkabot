package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;

@Component
public class StartDuel implements CommandExecutor {

    public StartDuel() {
    }

    @Override
    public String getTrigger() {
        return "/duel";
    }

    @Override
    public String getDescription() {
        return "Начать дуэль";
    }

    @Override
    public void execute(MessageContext ctx) {
        var user = ctx.user();
        var name = Html.htmlSafe(user.getFirstName());
        ctx.reply("🎯 Пользователь " + name + " начинает набор на дуэль!")
                .setReplyMarkup(new MarkupBuilder()
                        .addButton(ButtonBuilder.callbackButton()
                                .text("Присоединиться")
                                .payload(Callbacks.DUEL + " " + user.getId()))
                        .build())
                .callAsync(ctx.sender);
    }
}
