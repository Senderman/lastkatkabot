package com.senderman.lastkatkabot.callback;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class JoinDuel implements CallbackExecutor {

    private final UserStatsService users;

    public JoinDuel(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return Callbacks.DUEL;
    }

    @Override
    public void accept(CallbackQueryContext ctx) {
        var firstUserId = Long.parseLong(ctx.data().split("\\s+")[1]);
        var secondUser = ctx.user();
        if (secondUser.getId().equals(firstUserId)) {
            ctx.answer("👺 Похоже, вам надо обратиться к психологу! Вы пытаетесь вызвать на дуэль самого себя!", true)
                    .callAsync(ctx.sender);
            return;
        }
        var firstUserMember = Methods.getChatMember(ctx.message().getChatId(), firstUserId).call(ctx.sender);
        if (firstUserMember == null) {
            ctx.answer("😒 Похоже, ваш оппонент ушел из чата!", true).callAsync(ctx.sender);
            ctx.editMessage("😒 Дуэль не состоялась, так как один из дуэлянтов покинул чат!").callAsync(ctx.sender);
            return;
        }
        ctx.answer("Вы вступили в дуэль!").callAsync(ctx.sender);
        var firstUser = firstUserMember.getUser();
        var result = calculateResults(firstUser, secondUser);
        processDuelResultToDatabase(result);
        processDuelResultToMessage(ctx, result);
    }

    private DuelResult calculateResults(User user1, User user2) {
        var winResult = ThreadLocalRandom.current().nextInt(100);
        var winner = winResult < 50 ? user1 : user2;
        var loser = winResult < 50 ? user2 : user1;
        boolean draw = ThreadLocalRandom.current().nextInt(100) < 20;
        return new DuelResult(winner, loser, draw);
    }

    private void processDuelResultToMessage(CallbackQueryContext ctx, DuelResult result) {
        var winnerName = Html.htmlSafe(result.winner.getFirstName());
        var loserName = Html.htmlSafe(result.loser.getFirstName());
        var text = "<b>Итоги дуэли:</b>\n\n";
        if (result.isDraw()) {
            text += String.format("\uD83D\uDFE1 Ничья!\n\nУчастники: %s, %s", winnerName, loserName);
        } else {
            text += "\uD83D\uDE0E Победитель: " + winnerName + "\n" +
                    "\uD83D\uDE14 Проигравший: " + loserName;
        }
        ctx.editMessage(text).callAsync(ctx.sender);
    }

    private void processDuelResultToDatabase(DuelResult result) {
        var winner = result.winner;
        var loser = result.loser;
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
