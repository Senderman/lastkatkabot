package com.senderman.lastkatkabot.feature.love.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Singleton
public class DivorceCallback implements CallbackExecutor {

    public final static String NAME = "DIVORCE";

    private final UserStatsService users;

    public DivorceCallback(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(@NotNull L10nCallbackQueryContext ctx) {
        var userId = ctx.user().getId();

        if (!userId.equals(Long.parseLong(ctx.argument(1)))) {
            ctx.answerAsAlert(ctx.getString("love.divorce.notForYou")).callAsync(ctx.sender);
            return;
        }

        if (ctx.argument(0).equals("a"))
            acceptDivorce(ctx);
        else
            declineDivorce(ctx);


    }

    private void acceptDivorce(L10nCallbackQueryContext ctx) {
        var userId = ctx.user().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null || !loverId.equals(Long.parseLong(ctx.argument(2)))) {
            ctx.answerAsAlert(ctx.getString("love.divorce.pairChanged")).callAsync(ctx.sender);
            Methods.deleteMessage(ctx.message().getChatId(), ctx.message().getMessageId()).callAsync(ctx.sender);
            return;
        }

        var loverStats = users.findById(loverId);
        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.saveAll(List.of(userStats, loverStats));

        ctx.answerAsAlert(ctx.getString("love.divorce.notifySuccess")).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("love.divorce.notifySuccess"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
        Methods.sendMessage(loverId, ctx.getString("love.divorce.notifyLover")).callAsync(ctx.sender);
    }

    private void declineDivorce(L10nCallbackQueryContext ctx) {
        ctx.answer(ctx.getString("love.divorce.cancelNotify")).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("love.divorce.canceled"))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }
}
