package com.senderman.lastkatkabot.feature.roleplay.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CallbackExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nCallbackQueryContext;
import com.senderman.lastkatkabot.feature.userstats.model.UserStats;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class JoinDuelCallback implements CallbackExecutor {

    public final static String NAME = "DUEL";

    private final UserStatsService users;

    public JoinDuelCallback(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return NAME;
    }

    @Override
    public void accept(L10nCallbackQueryContext ctx) {
        var firstUserId = Long.parseLong(ctx.argument(0));
        var secondUser = ctx.user();
        if (secondUser.getId().equals(firstUserId)) {
            ctx.answer(ctx.getString("roleplay.duel.selfDuel"), true)
                    .callAsync(ctx.sender);
            return;
        }
        var firstUserMember = Methods.getChatMember(ctx.message().getChatId(), firstUserId).call(ctx.sender);
        if (firstUserMember == null) {
            ctx.answer(ctx.getString("roleplay.duel.leftChatNotify"), true).callAsync(ctx.sender);
            ctx.editMessage(ctx.getString("roleplay.duel.leftChatMessage"))
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
            return;
        }
        ctx.answer(ctx.getString("roleplay.duel.joinNotify")).callAsync(ctx.sender);
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

    private void processDuelResultToMessage(L10nCallbackQueryContext ctx, DuelResult result) {
        var winnerName = Html.htmlSafe(result.winner.getFirstName());
        var loserName = Html.htmlSafe(result.loser.getFirstName());
        var text = ctx.getString("roleplay.duel.resultTitle");
        if (result.isDraw()) {
            text += ctx.getString("roleplay.duel.resultDraw").formatted(winnerName, loserName);
        } else {
            text += ctx.getString("roleplay.duel.resultMessage").formatted(winnerName, loserName);
        }
        ctx.editMessage(text)
                .disableWebPagePreview()
                .callAsync(ctx.sender);
    }

    private void processDuelResultToDatabase(DuelResult result) {
        var winner = result.winner;
        var loser = result.loser;
        UserStats winnerStats = users.findById(winner.getId());
        UserStats loserStats = users.findById(loser.getId());
        winnerStats.increaseDuelsTotal();
        loserStats.increaseDuelsTotal();
        if (!result.isDraw())
            winnerStats.increaseDuelWins();

        users.saveAll(List.of(winnerStats, loserStats));
    }

    private record DuelResult(User winner, User loser, boolean isDraw) {
    }
}
