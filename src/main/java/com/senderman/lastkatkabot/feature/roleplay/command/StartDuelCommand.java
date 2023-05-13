package com.senderman.lastkatkabot.feature.roleplay.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

@Command
public class StartDuelCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/duel";
    }

    @Override
    public String getDescription() {
        return "roleplay.duel.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var user = ctx.user();
        var name = Html.htmlSafe(user.getFirstName());
        ctx.reply(ctx.getString("roleplay.duel.startMessage").formatted(name))
                .setInlineKeyboard(ButtonBuilder.callbackButton()
                        .text(ctx.getString("roleplay.duel.joinButton"))
                        .payload(JoinDuelCallback.NAME, user.getId())
                        .create())
                .callAsync(ctx.sender);
    }
}
