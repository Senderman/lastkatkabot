package com.senderman.lastkatkabot.feature.love.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

@Command
public class DivorceCommand implements CommandExecutor {

    private final UserStatsService users;

    public DivorceCommand(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return "/divorce";
    }

    @Override
    public String getDescriptionKey() {
        return "love.divorce.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {

        var userId = ctx.user().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            ctx.replyToMessage(ctx.getString("love.divorce.noLover")).callAsync(ctx.sender);
            return;
        }

        ctx.replyToMessage(ctx.getString("love.divorce.areYouSure"))
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton(ctx.getString("love.divorce.divorceButton"))
                                .payload(DivorceCallback.NAME, "a", userId, loverId)
                                .create(),
                        ButtonBuilder.callbackButton(ctx.getString("love.divorce.cancelButton"))
                                .payload(DivorceCallback.NAME, "d", userId)
                                .create()
                )
                .callAsync(ctx.sender);

    }
}
