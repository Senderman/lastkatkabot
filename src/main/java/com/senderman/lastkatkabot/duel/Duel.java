package com.senderman.lastkatkabot.duel;

import org.jetbrains.annotations.Contract;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.concurrent.ThreadLocalRandom;

public class Duel {

    private final User user1;

    @Contract()
    public Duel(User user1) {
        this.user1 = user1;
    }

    public DuelResult run(User user2) {

        var winResult = ThreadLocalRandom.current().nextInt(100);
        var winner = winResult < 50 ? user1 : user2;
        var loser = winResult < 50 ? user2 : user1;
        boolean draw = ThreadLocalRandom.current().nextInt(100) < 20;
        return new DuelResult(winner, loser, draw);
    }

    public User getUser1() {
        return user1;
    }

    static class DuelResult {

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
