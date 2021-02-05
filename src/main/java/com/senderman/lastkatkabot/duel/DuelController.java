package com.senderman.lastkatkabot.duel;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
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
    private final CommonAbsSender telegram;

    public DuelController(UserStatsRepository users, CommonAbsSender telegram) {
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

        var duel = duels.get(key);
        if (duel.getUser1().getId().equals(user.getId())) {
            throw new SameUserException(user.getId());
        }

        duels.remove(key);
        var result = duel.run(user);
        processDuelResultToDatabase(result);
        processDuelResultToMessage(chatId, messageId, result);
    }

    private void processDuelResultToMessage(long chatId, int messageId, Duel.DuelResult result) {
        var method = Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setReplyMarkup(null)
                .enableHtml();

        var winnerName = Html.htmlSafe(result.getWinner().getFirstName());
        var loserName = Html.htmlSafe(result.getLoser().getFirstName());
        var text = "<b>Итоги дуэли:</b>\n\n";
        if (result.isDraw()) {
            text += String.format("\uD83D\uDFE1 Ничья!\n\nУчастники: %s, %s", winnerName, loserName);
        } else {
            text += "\uD83D\uDE0E Победитель: " + winnerName + "\n" +
                    "\uD83D\uDE14 Проигравший: " + loserName;
        }
        method.setText(text);
        method.callAsync(telegram);
    }

    private void processDuelResultToDatabase(Duel.DuelResult result) {
        var winner = result.getWinner();
        var loser = result.getLoser();
        Userstats winnerStats = users.findById(winner.getId()).orElseGet(()->new Userstats(winner.getId()));
        Userstats loserStats = users.findById(loser.getId()).orElseGet(()->new Userstats(loser.getId()));
        winnerStats.increaseDuelsTotal();
        loserStats.increaseDuelsTotal();
        if (!result.isDraw())
            winnerStats.increaseDuelWins();

        users.saveAll(List.of(winnerStats, loserStats));
    }

    private String createKey(long chatId, int messageId) {
        return chatId + " " + messageId;
    }

}
