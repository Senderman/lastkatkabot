package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.dbservice.MarriageRequestService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarriageCallback implements CallbackExecutor {

    private final UserStatsService userStats;
    private final MarriageRequestService marriages;

    public MarriageCallback(UserStatsService userStats, MarriageRequestService marriages) {
        this.userStats = userStats;
        this.marriages = marriages;
    }

    @Override
    public String getTrigger() {
        return Callbacks.MARRIAGE;
    }

    @Override
    public void execute(CallbackQueryContext ctx) {
        if (ctx.data().endsWith("accept"))
            acceptMarriage(ctx);
        else
            declineMarriage(ctx);
    }

    private void acceptMarriage(CallbackQueryContext ctx) {
        var requestOptional = marriages.findById(Integer.parseInt(ctx.data().split("\\s+")[1]));
        if (requestOptional.isEmpty()) {
            ctx.answerAsAlert("Вашу заявку потеряли в ЗАГСе!").callAsync(ctx.sender);
            ctx.editMessage("К сожалению, в ЗАГСе потеряли вашу запись. Попробуйте еще раз").callAsync(ctx.sender);
            return;
        }
        var r = requestOptional.get();
        // query user id should match with proposee id
        if (!ctx.user().getId().equals(r.getProposeeId())) {
            ctx.answerAsAlert("Это не вам!").callAsync(ctx.sender);
            return;
        }
        var proposeeStats = userStats.findById(r.getProposeeId());
        // proposee should not have lover
        if (proposeeStats.hasLover()) {
            ctx.answerAsAlert("Вы уже имеете вторую половинку!").callAsync(ctx.sender);
            ctx.editMessage("Пользователь " + r.getProposeeName() + " уже имеет вторую половинку!").callAsync(ctx.sender);
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
        // all marriage request with these ones are obsolete now
        marriages.deleteByProposerIdOrProposeeId(r.getProposerId(), r.getProposeeId());
        userStats.saveAll(List.of(proposerStats, proposeeStats));
        ctx.answer("Вы приняли предложение!").callAsync(ctx.sender);
        ctx.editMessage("Пользователь " + r.getProposeeName() + " принял предложение!").callAsync(ctx.sender);
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

    private void declineMarriage(CallbackQueryContext ctx) {
        var requestId = Integer.parseInt(ctx.data().split("\\s+")[1]);
        marriages.deleteById(requestId);
        ctx.answer("Вы отказались от брака!").callAsync(ctx.sender);
        ctx.editMessage("Пользователь " + Html.getUserLink(ctx.user()) + " отказался от брака!").callAsync(ctx.sender);
    }
}
