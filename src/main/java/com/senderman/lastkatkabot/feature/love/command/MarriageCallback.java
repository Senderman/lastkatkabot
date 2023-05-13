package com.senderman.lastkatkabot.feature.love.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.localization.context.LocalizedCallbackQueryContext;
import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;
import com.senderman.lastkatkabot.feature.love.service.MarriageRequestService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class MarriageCallback implements CallbackExecutor {

    public final static String NAME = "MARRIAGE";

    private final UserStatsService userStats;
    private final MarriageRequestService marriages;

    public MarriageCallback(UserStatsService userStats, MarriageRequestService marriages) {
        this.userStats = userStats;
        this.marriages = marriages;
    }

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(LocalizedCallbackQueryContext ctx) {
        var requestId = Integer.parseInt(ctx.argument(1));
        var requestOptional = marriages.findById(requestId);

        if (requestOptional.isEmpty()) {
            ctx.answerAsAlert(ctx.getString("love.marriage.errorNotify")).callAsync(ctx.sender);
            ctx.editMessage(ctx.getString("love.marriage.errorMessage"))
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
            return;
        }

        var r = requestOptional.get();
        // query user id should match with proposee id
        if (!ctx.user().getId().equals(r.getProposeeId())) {
            ctx.answerAsAlert(ctx.getString("love.marriage.notForYou")).callAsync(ctx.sender);
            return;
        }


        if (ctx.argument(0).equals("accept"))
            acceptMarriage(ctx, r);
        else
            declineMarriage(ctx, r);
    }

    private void acceptMarriage(LocalizedCallbackQueryContext ctx, MarriageRequest r) {

        var proposeeStats = userStats.findById(r.getProposeeId());
        // proposee should not have lover
        if (proposeeStats.hasLover()) {
            ctx.answerAsAlert(ctx.getString("love.marriage.proposeeHasLoverNotify")).callAsync(ctx.sender);
            ctx.editMessage(ctx.getString("love.marriage.proposeeHasLoverMessage").formatted(r.getProposeeName()))
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
            marriages.delete(r);
            return;
        }
        var proposerStats = userStats.findById(r.getProposerId());
        // proposer also should not have lover
        if (proposerStats.hasLover()) {
            ctx.answerAsAlert(ctx.getString("love.marriage.proposerHasLoverNotify")).callAsync(ctx.sender);
            ctx.answerAsAlert(ctx.getString("love.marriage.proposerHasLoverMessage").formatted(r.getProposerName())).callAsync(ctx.sender);
            marriages.delete(r);
            return;
        }
        // if everything is ok, proceed to marriage
        proposerStats.setLoverId(r.getProposeeId());
        proposeeStats.setLoverId(r.getProposerId());
        // all marriage request with these are obsolete now
        marriages.deleteByProposerIdOrProposeeId(r.getProposerId(), r.getProposeeId());
        userStats.saveAll(List.of(proposerStats, proposeeStats));
        ctx.answer(ctx.getString("love.marriage.acceptNotify")).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("love.marriage.acceptMessage").formatted(r.getProposeeName()))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
        Methods.sendMessage()
                .setChatId(r.getProposerId())
                .setText(ctx.getString("love.marriage.acceptMessage").formatted(r.getProposeeName()))
                .callAsync(ctx.sender);

        Methods.sendMessage()
                .setChatId(ctx.message().getChatId())
                .setText(String.format(ctx.getString("love.marriage.message"),
                        r.getProposerName(), r.getProposeeName()))
                .callAsync(ctx.sender);
    }

    private void declineMarriage(LocalizedCallbackQueryContext ctx, MarriageRequest r) {
        marriages.delete(r);
        ctx.answer(ctx.getString("love.marriage.cancelNotify")).callAsync(ctx.sender);
        ctx.editMessage(ctx.getString("love.marriage.cancelMessage").formatted(Html.getUserLink(ctx.user())))
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }
}
