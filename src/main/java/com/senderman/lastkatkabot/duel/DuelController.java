package com.senderman.lastkatkabot.duel;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class DuelController {

    private final Map<String, Duel> duels = new HashMap<>(); // key is "chatId messageId"
    private final UserStatsRepository users;
    private final ApiRequests telegram;

    public DuelController(UserStatsRepository users, ApiRequests telegram) {
        this.users = users;
        this.telegram = telegram;
    }

    public void newDuel(long chatId, int messageId, User user) {
        var key = createKey(chatId, messageId);
        duels.put(key, new Duel(user));
    }

    public void joinDuel(long chatId, int messageId, User user) throws SameUserException, NoSuchElementException {
        var key = createKey(chatId, messageId);
        if (!duels.containsKey(key))
            throw new NoSuchElementException("No duel with given key \"" + key + "\" in memory");

        var result = processDuelResultToDatabase(key, user);
        var method = Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setReplyMarkup(null)
                .enableHtml();

        var winnerName = Html.htmlSafe(result.getWinner().getFirstName());
        var loserName = Html.htmlSafe(result.getLoser().getFirstName());
        var text = "<b>Итоги дуэли:</b>\n\n";
        if (result.isDraw()) {
            text += "\uD83D\uDFE1 Ничья!";
        } else {
            text += "\uD83D\uDE0E Победитель: " + winnerName + "\n" +
                    "\uD83D\uDE14 Проигравший: " + loserName;
        }
        method.setText(text);
        telegram.execute(method);

    }

    private Duel.DuelResult processDuelResultToDatabase(String key, User user) throws SameUserException {
        var duel = duels.get(key);
        if (duel.getUser1().getId().equals(user.getId()))
            throw new SameUserException(user.getId());

        duels.remove(key);
        var results = duel.run(user);
        var winner = results.getWinner();
        var loser = results.getLoser();
        Userstats winnerStats = users.findById(winner.getId()).orElse(new Userstats(winner.getId()));
        Userstats loserStats = users.findById(loser.getId()).orElse(new Userstats(loser.getId()));
        winnerStats.increaseDuelsTotal();
        loserStats.increaseDuelsTotal();
        if (!results.isDraw())
            winnerStats.increaseDuelWins();
        users.saveAll(List.of(winnerStats, loserStats));
        return results;
    }

    private String createKey(long chatId, int messageId) {
        return chatId + " " + messageId;
    }

}
