package com.senderman.lastkatkabot.feature.love.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.command.CallbackExecutor;
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
    public void accept(CallbackQueryContext ctx) {
        var requestId = Integer.parseInt(ctx.argument(1));
        var requestOptional = marriages.findById(requestId);

        if (requestOptional.isEmpty()) {
            ctx.answerAsAlert("Вашу заявку потеряли в ЗАГСе!").callAsync(ctx.sender);
            ctx.editMessage("К сожалению, в ЗАГСе потеряли вашу запись. Попробуйте еще раз")
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
            return;
        }

        var r = requestOptional.get();
        // query user id should match with proposee id
        if (!ctx.user().getId().equals(r.getProposeeId())) {
            ctx.answerAsAlert("Это не вам!").callAsync(ctx.sender);
            return;
        }


        if (ctx.argument(0).equals("accept"))
            acceptMarriage(ctx, r);
        else
            declineMarriage(ctx, r);
    }

    private void acceptMarriage(CallbackQueryContext ctx, MarriageRequest r) {

        var proposeeStats = userStats.findById(r.getProposeeId());
        // proposee should not have lover
        if (proposeeStats.hasLover()) {
            ctx.answerAsAlert("Вы уже имеете вторую половинку!").callAsync(ctx.sender);
            ctx.editMessage("Пользователь " + r.getProposeeName() + " уже имеет вторую половинку!")
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
            marriages.delete(r);
            return;
        }
        var proposerStats = userStats.findById(r.getProposerId());
        // proposer also should not have lover
        if (proposerStats.hasLover()) {
            ctx.answerAsAlert("Слишком поздно, у пользователя уже есть другой!").callAsync(ctx.sender);
            ctx.answerAsAlert("Пользователь " + r.getProposerName() + " уже имеет вторую половинку!").callAsync(ctx.sender);
            marriages.delete(r);
            return;
        }
        // if everything is ok, proceed to marriage
        proposerStats.setLoverId(r.getProposeeId());
        proposeeStats.setLoverId(r.getProposerId());
        // all marriage request with these are obsolete now
        marriages.deleteByProposerIdOrProposeeId(r.getProposerId(), r.getProposeeId());
        userStats.saveAll(List.of(proposerStats, proposeeStats));
        ctx.answer("Вы приняли предложение!").callAsync(ctx.sender);
        ctx.editMessage("Пользователь " + r.getProposeeName() + " принял предложение!")
                .disableWebPagePreview()
                .callAsync(ctx.sender);
        Methods.sendMessage()
                .setChatId(r.getProposerId())
                .setText("Пользователь " + r.getProposeeName() + " принял предложение!")
                .callAsync(ctx.sender);

        Methods.sendMessage()
                .setChatId(ctx.message().getChatId())
                .setText(String.format("💐 У %s и %s свадьба! Давайте их поздравим и съедим шавуху 🌯 !!!",
                        r.getProposerName(), r.getProposeeName()))
                .callAsync(ctx.sender);
    }

    private void declineMarriage(CallbackQueryContext ctx, MarriageRequest r) {
        marriages.delete(r);
        ctx.answer("Вы отказались от брака!").callAsync(ctx.sender);
        ctx.editMessage("Пользователь " + Html.getUserLink(ctx.user()) + " отказался от брака!")
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }
}
