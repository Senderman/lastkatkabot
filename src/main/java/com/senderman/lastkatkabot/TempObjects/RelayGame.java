package com.senderman.lastkatkabot.TempObjects;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RelayGame {
    public final List<Integer> players;
    private final long chatId;
    private final Map<Integer, List<String>> playerWords;
    public boolean isGoing;
    public boolean needToAskLeader;
    public int leaderId;
    private int length;

    public RelayGame(Message message) {
        chatId = message.getChatId();
        int length;
        try {
            length = Integer.parseInt(message.getText().split(" ")[1]);
            if (length < 4 || length > 10)
                this.length = 4;
            else
                this.length = length;
        } catch (Exception e) {
            this.length = 4;
        }
        isGoing = false;
        needToAskLeader = false;
        players = new ArrayList<>();
        playerWords = new HashMap<>();
        Services.handler().sendMessage(chatId, "Начат набор в игру \"Эстафета\"!\n" +
                "Используйте команды /joinrelay, /leaverelay и /startrelay!");
    }

    public static String relayHelp() {
        return "Эта игра была изобретена весной 1999 года в школе N 590 Санкт-Петербурга\n" +
                "Выбранный ботом участник пишет в чат слово. Остальные за 5 минут должны последовательно отправлять боту" +
                "слова заданной длины, причем каждое новое слово должно содержать ровно одну букву из предыдущего." +
                "Побеждает тот, кто придумает больше всего слов";
    }

    public void askLeader() {
        if (players.size() < 2) {
            Services.handler().sendMessage(chatId, "Недостаточно игроков!");
            return;
        }
        isGoing = true;
        needToAskLeader = true;
        leaderId = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        var leaderName = Methods.getChatMember(chatId, leaderId).call(Services.handler()).getUser().getFirstName();
        var leader = new TgUser(leaderId, leaderName);
        Services.handler().sendMessage(chatId, leader.getLink() + ", пожалуйста," +
                "напишите мне в лс любое слово, без повторяющихся букв, длина слова - " + length);
    }

    public void checkLeaderWord(Message message) {
        var word = message.getText().toLowerCase();
        if (word.length() != length) {
            Services.handler().sendMessage(leaderId, "Неверная длина слова!");
            return;
        }
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; i++) {
                if (word.charAt(i) == word.charAt(j)) {
                    Services.handler().sendMessage(leaderId, "Слово не должно содержать повторяющихся букв!");
                    return;
                }
            }
        }
        Services.handler().sendMessage(leaderId, "Слово принято!");
        for (var w : playerWords.values()) {
            w.add(word);
        }
        needToAskLeader = false;
        Services.handler().sendMessage(chatId, "Ведущий назвал слово - <b>" + word + "</b>!");
        startGame();
    }

    private void startGame() {
        new Thread(() -> {
            try {
                runTimer();
            } catch (InterruptedException e) {
                Services.handler().sendMessage(chatId, "Игра отменена из-за ошибки таймера :(");
                Services.handler().relayGames.remove(chatId);
            }
        }).start();
    }

    public void checkWord(Message message) {
        var userId = message.getFrom().getId();
        var word = message.getText().toLowerCase();
        if (playerWords.get(userId).contains(word)) {
            Services.handler().sendMessage(userId, "Слова повторять нельзя!");
            return;
        }
        if (word.length() != length) {
            Services.handler().sendMessage(userId, "Неверная длина слова!");
            return;
        }
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; i++) {
                if (word.charAt(i) == word.charAt(j)) {
                    Services.handler().sendMessage(userId, "Слово не должно содержать повторяющихся букв!");
                    return;
                }
            }
        }
        // check that word contains only one letter from previous word
        var words = playerWords.get(userId);
        if (words.size() == 0)
            return;
        var lastWord = words.get(words.size() - 1);
        lastWord = lastWord.replaceAll("[" + word + "]+", "");
        if (lastWord.length() != length - 1) {
            Services.handler().sendMessage(userId, "Слово не принято!");
        } else {
            Services.handler().sendMessage(userId, "Слово принято!");
            words.add(word);
        }
    }

    private void endGame() {
        isGoing = false;
        for (var player : players) {
            Services.handler().sendMessage(player, "Игра окончена! Смотрите в чат игры для получения результатов!");
        }
        var history = new StringBuilder("<b>Результаты игры:</b>\n\n");
        for (var playerId : players) {
            var playerName = Methods.getChatMember(chatId, playerId).call(Services.handler()).getUser().getFirstName();
            var player = new TgUser(playerId, playerName);
            history.append(player.getLink()).append(":\n");
            for (var word : playerWords.get(playerId)) {
                history.append(word).append("\n");
            }
            history.append("Всего слов: ").append(playerWords.get(playerId).size()).append("\n");
        }
        Services.handler().sendMessage(chatId, history.toString());
        Services.handler().relayGames.remove(chatId);
    }

    private void runTimer() throws InterruptedException {
        Services.handler().sendMessage(chatId, "Набор игроков закончен! Старт через 3...");
        Thread.sleep(1000);
        Services.handler().sendMessage(chatId, "2..");
        Thread.sleep(1000);
        Services.handler().sendMessage(chatId, "1..");
        Thread.sleep(1000);
        Services.handler().sendMessage(chatId, "Вперед! Пишите мне в лс ваши слова! У вас есть 5 минут");
        Thread.sleep(300000);
        endGame();
    }

    public void addPlayer(Message message) {
        var userId = message.getFrom().getId();
        if (players.contains(userId)) {
            Services.handler().sendMessage(chatId, "Вы уже джойнулись!");
            return;
        }
        try {
            Methods.sendMessage(userId, "Вы успешно присоеднились!").call(Services.handler());
            players.add(userId);
            playerWords.put(userId, new ArrayList<>());
            Services.handler().sendMessage(chatId, message.getFrom().getFirstName() + " присоденился!");
        } catch (Exception e) {
            Services.handler().sendMessage(chatId, "Сначала напишите мне в лс!");
        }
    }

    public void kickPlayer(Message message) {
        var userId = message.getFrom().getId();
        if (!players.contains(userId)) {
            Services.handler().sendMessage(chatId, "Вы не в игре!");
            return;
        }
        players.remove(userId);
        playerWords.remove(userId);
        Services.handler().sendMessage(chatId, message.getFrom().getFirstName() + "покинул игру!");
    }
}
