package com.senderman.lastkatkabot.feature.love.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedMessageContext;
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
    public String getDescription() {
        return "love.divorce.description";
    }

    @Override
    public void accept(@NotNull LocalizedMessageContext ctx) {

        var userId = ctx.user().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            ctx.replyToMessage(ctx.getString("love.divorce.noLover")).callAsync(ctx.sender);
            return;
        }

        ctx.replyToMessage(ctx.getString("love.divorce.areYouSure"))
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("love.divorce.divorceButton"))
                                .payload(DivorceCallback.NAME, "a", userId, loverId)
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("love.divorce.cancelButton"))
                                .payload(DivorceCallback.NAME, "d", userId)
                                .create()
                )
                .callAsync(ctx.sender);

    }
}
