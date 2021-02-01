package com.senderman.lastkatkabot.callback;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
public class MarriageCallback implements CallbackExecutor {

    private final ApiRequests telegram;
    private final UserStatsRepository userStats;
    private final MarriageRequestRepository marriages;

    public MarriageCallback(ApiRequests telegram, UserStatsRepository userStats, MarriageRequestRepository marriages) {
        this.telegram = telegram;
        this.userStats = userStats;
        this.marriages = marriages;
    }

    @Override
    public String getTrigger() {
        return Callbacks.MARRIAGE;
    }

    @Override
    public void execute(CallbackQuery query) {
        if (query.getData().endsWith("accept"))
            acceptMarriage(query);
        else
            declineMarriage(query);
    }

    private void acceptMarriage(CallbackQuery query) {
        var requestOptional = marriages.findById(Integer.parseInt(query.getData().split("\\s+")[1]));
        if (requestOptional.isEmpty()) {
            telegram.answerCallbackQuery(query, "Вашу заявку потеряли в ЗАГСе!", true);
            telegram.editQueryMessage(query, "К сожалению, в ЗАГСе потеряли вашу запись. Попробуйте еще раз");
            return;
        }
        var r = requestOptional.get();
        // query user id should match with proposee id
        if (!query.getFrom().getId().equals(r.getProposeeId())) {
            telegram.answerCallbackQuery(query, "Это не вам!", true);
            return;
        }
        var proposeeStats = userStats.findById(r.getProposeeId()).orElseGet(() -> new Userstats(r.getProposeeId()));
        // proposee should not have lover
        if (proposeeStats.hasLover()) {
            telegram.answerCallbackQuery(query, "Вы уже имеете вторую половинку!", true);
            telegram.editQueryMessage(query, "Пользователь " + r.getProposeeName() + " уже имеет вторую половинку!");
            marriages.delete(r);
            return;
        }
        var proposerStats = userStats.findById(r.getProposerId()).orElseGet(() -> new Userstats(r.getProposerId()));
        // proposer also should not have lover
        if (proposerStats.hasLover()) {
            telegram.answerCallbackQuery(query, "Слишком поздно, у пользователя уже есть другой!", true);
            telegram.editQueryMessage(query, "Пользователь " + r.getProposerName() + " уже имеет вторую половинку!");
            marriages.delete(r);
            return;
        }
        // if everything is ok, proceed to marriage
        proposerStats.setLoverId(r.getProposeeId());
        proposeeStats.setLoverId(r.getProposerId());
        // all marriage request with these ones are obsolete now
        marriages.deleteByProposerIdOrProposeeId(r.getProposerId(), r.getProposeeId());
        userStats.saveAll(List.of(proposerStats, proposeeStats));
        telegram.answerCallbackQuery(query, "Вы приняли предложение!", true);
        telegram.editQueryMessage(query, "Пользователь " + r.getProposeeName() + " принял предложение!");
        telegram.sendMessage(r.getProposerId(), "Пользователь " + r.getProposeeName() + " принял предложение!");
        telegram.sendMessage(query.getMessage().getChatId(),
                String.format("\uD83D\uDC90 У %s и %s свадьба! Давайте их поздравим и съедим шавуху \uD83C\uDF2F !!!",
                        r.getProposerName(), r.getProposeeName()));
    }

    private void declineMarriage(CallbackQuery query) {
        var requestId = Integer.parseInt(query.getData().split("\\s+")[1]);
        marriages.deleteById(requestId);
        telegram.answerCallbackQuery(query, "Вы отказались от брака!");
        telegram.editQueryMessage(query,
                "Пользователь " + Html.getUserLink(query.getFrom()) + " отказался от брака!"
        );
    }
}
