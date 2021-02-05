package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
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

    private final CommonAbsSender telegram;
    private final UserStatsRepository userStats;
    private final MarriageRequestRepository marriages;

    public MarriageCallback(CommonAbsSender telegram, UserStatsRepository userStats, MarriageRequestRepository marriages) {
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
            ApiRequests.answerCallbackQuery(query, "Вашу заявку потеряли в ЗАГСе!", true).callAsync(telegram);
            ApiRequests.editMessage(query, "К сожалению, в ЗАГСе потеряли вашу запись. Попробуйте еще раз").callAsync(telegram);
            return;
        }
        var r = requestOptional.get();
        // query user id should match with proposee id
        if (!query.getFrom().getId().equals(r.getProposeeId())) {
            ApiRequests.answerCallbackQuery(query, "Это не вам!").callAsync(telegram);
            return;
        }
        var proposeeStats = userStats.findById(r.getProposeeId()).orElseGet(() -> new Userstats(r.getProposeeId()));
        // proposee should not have lover
        if (proposeeStats.hasLover()) {
            ApiRequests.answerCallbackQuery(query, "Вы уже имеете вторую половинку!", true).callAsync(telegram);
            ApiRequests.editMessage(query, "Пользователь " + r.getProposeeName() + " уже имеет вторую половинку!").callAsync(telegram);
            marriages.delete(r);
            return;
        }
        var proposerStats = userStats.findById(r.getProposerId()).orElseGet(() -> new Userstats(r.getProposerId()));
        // proposer also should not have lover
        if (proposerStats.hasLover()) {
            ApiRequests.answerCallbackQuery(query, "Слишком поздно, у пользователя уже есть другой!", true).callAsync(telegram);
            ApiRequests.editMessage(query, "Пользователь " + r.getProposerName() + " уже имеет вторую половинку!").callAsync(telegram);
            marriages.delete(r);
            return;
        }
        // if everything is ok, proceed to marriage
        proposerStats.setLoverId(r.getProposeeId());
        proposeeStats.setLoverId(r.getProposerId());
        // all marriage request with these ones are obsolete now
        marriages.deleteByProposerIdOrProposeeId(r.getProposerId(), r.getProposeeId());
        userStats.saveAll(List.of(proposerStats, proposeeStats));
        ApiRequests.answerCallbackQuery(query, "Вы приняли предложение!", true).callAsync(telegram);
        ApiRequests.editMessage(query, "Пользователь " + r.getProposeeName() + " принял предложение!").callAsync(telegram);
        Methods.sendMessage()
                .setChatId(r.getProposerId())
                .setText("Пользователь " + r.getProposeeName() + " принял предложение!")
                .callAsync(telegram);

        Methods.sendMessage()
                .setChatId(query.getMessage().getChatId())
                .setText(String.format("\uD83D\uDC90 У %s и %s свадьба! Давайте их поздравим и съедим шавуху \uD83C\uDF2F !!!",
                        r.getProposerName(), r.getProposeeName()))
                .callAsync(telegram);
    }

    private void declineMarriage(CallbackQuery query) {
        var requestId = Integer.parseInt(query.getData().split("\\s+")[1]);
        marriages.deleteById(requestId);
        ApiRequests.answerCallbackQuery(query, "Вы отказались от брака!").callAsync(telegram);
        ApiRequests.editMessage(query,
                "Пользователь " + Html.getUserLink(query.getFrom()) + " отказался от брака!").callAsync(telegram);
    }
}
