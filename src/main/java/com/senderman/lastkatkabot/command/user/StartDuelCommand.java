package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;

@Command(
        command = "/duel",
        description = "Начать дуэль"
)
public class StartDuelCommand extends CommandExecutor {

    public StartDuelCommand() {
    }

    @Override
    public void accept(MessageContext ctx) {
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
