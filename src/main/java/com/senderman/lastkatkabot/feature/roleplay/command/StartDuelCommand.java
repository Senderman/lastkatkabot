package com.senderman.lastkatkabot.feature.roleplay.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;

@Command
public class StartDuelCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/duel";
    }

    @Override
    public String getDescription() {
        return "начать дуэль";
    }

    @Override
    public void accept(MessageContext ctx) {
        var user = ctx.user();
        var name = Html.htmlSafe(user.getFirstName());
        ctx.reply("🎯 Пользователь " + name + " начинает набор на дуэль!")
                .setInlineKeyboard(ButtonBuilder.callbackButton()
                        .text("Присоединиться")
                        .payload(JoinDuelCallback.NAME, user.getId())
                        .create())
                .callAsync(ctx.sender);
    }
}
