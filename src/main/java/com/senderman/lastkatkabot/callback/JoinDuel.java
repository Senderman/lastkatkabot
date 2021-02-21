package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class JoinDuel implements CallbackExecutor {

    private final UserStatsService users;
    private final CommonAbsSender telegram;

    public JoinDuel(UserStatsService users, CommonAbsSender telegram) {
        this.users = users;
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return Callbacks.DUEL;
    }

    @Override
    public void execute(CallbackQuery query) {
        var message = query.getMessage();
        var firstUserId = Integer.parseInt(query.getData().split("\\s+")[1]);
        var secondUser = query.getFrom();
        if (secondUser.getId().equals(firstUserId)) {
            ApiRequests.answerCallbackQuery(query,
                    "\uD83D\uDC7A Похоже, вам надо обратиться к психологу! Вы пытаетесь вызвать на дуэль самого себя!",
                    true).callAsync(telegram);
            return;
        }
        var firstUserMember = Methods.getChatMember(message.getChatId(), firstUserId).call(telegram);
        if (firstUserMember == null) {
            ApiRequests.answerCallbackQuery(query, "\uD83D\uDE12 Похоже, ваш оппонент ушел из чата!", true)
                    .callAsync(telegram);
            ApiRequests.editMessage(query, "\uD83D\uDE12 Дуэль не состоялась, так как один из дуэлянтов покинул чат!")
                    .callAsync(telegram);
            return;
        }
        ApiRequests.answerCallbackQuery(query, "Вы вступили в дуэль!").callAsync(telegram);
        var firstUser = firstUserMember.getUser();
        var result = calculateResults(firstUser, secondUser);
        processDuelResultToDatabase(result);
        processDuelResultToMessage(message.getChatId(), message.getMessageId(), result);
    }

    private DuelResult calculateResults(User user1, User user2) {
        var winResult = ThreadLocalRandom.current().nextInt(100);
        var winner = winResult < 50 ? user1 : user2;
        var loser = winResult < 50 ? user2 : user1;
        boolean draw = ThreadLocalRandom.current().nextInt(100) < 20;
        return new DuelResult(winner, loser, draw);
    }

    private void processDuelResultToMessage(long chatId, int messageId, DuelResult result) {
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

    private void processDuelResultToDatabase(DuelResult result) {
        var winner = result.getWinner();
        var loser = result.getLoser();
        Userstats winnerStats = users.findById(winner.getId());
        Userstats loserStats = users.findById(loser.getId());
        winnerStats.increaseDuelsTotal();
        loserStats.increaseDuelsTotal();
        if (!result.isDraw())
            winnerStats.increaseDuelWins();

        users.saveAll(List.of(winnerStats, loserStats));
    }

    private static class DuelResult {

        private final User winner;
        private final User loser;
        private final boolean isDraw;

        public DuelResult(User winner, User loser, boolean isDraw) {
            this.winner = winner;
            this.loser = loser;
            this.isDraw = isDraw;
        }

        public User getWinner() {
            return winner;
        }

        public User getLoser() {
            return loser;
        }

        public boolean isDraw() {
            return isDraw;
        }
    }
}
