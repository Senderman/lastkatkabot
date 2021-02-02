package com.senderman.lastkatkabot.bnc;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BncTelegramHandler {

    private final ApiRequests telegram;
    private final BncGamesManager gamesManager;
    private final UserStatsRepository usersRepo;
    private final Map<Long, List<Integer>> messagesToDelete;

    public BncTelegramHandler(
            ApiRequests telegram,
            @Qualifier("bncManagerDatabaseWrapper") BncGamesManager gamesManager,
            UserStatsRepository usersRepo
    ) {
        this.telegram = telegram;
        this.gamesManager = gamesManager;
        this.usersRepo = usersRepo;
        this.messagesToDelete = new HashMap<>();
    }

    public void processBncAnswer(Message message) {
        var chatId = message.getChatId();
        var number = message.getText().toUpperCase(Locale.ENGLISH);
        try {
            var result = gamesManager.check(chatId, number);
            addMessageToDelete(message);
            if (result.isWin()) {
                processWin(message, result);
            } else {
                sendGameMessage(chatId, formatResult(result));
            }
        } catch (NumberAlreadyCheckedException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Уже проверяли! " + formatResult(e.getResult()));
        } catch (RepeatingDigitsException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Число не должно иметь повторяющихся цифр!");
        } catch (GameOverException e) {
            addMessageToDelete(message);
            processGameOver(message, e.getAnswer());
        } catch (InvalidLengthException | InvalidCharacterException | NoSuchElementException ignored) {
        }
    }

    public BncGameState getGameState(long chatId) {
        return gamesManager.getGameState(chatId);
    }

    public boolean createGameIfNotExists(long chatId, int length, boolean isHexadecimal) {
        return gamesManager.createGameIfNotExists(chatId, length, isHexadecimal);
    }

    // Send message that will be deleted after game end
    public void sendGameMessage(long chatId, String text) {
        addMessageToDelete(telegram.sendMessage(chatId, text));
    }

    private void addMessageToDelete(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var list = messagesToDelete.computeIfAbsent(chatId, k -> new ArrayList<>());
        list.add(messageId);
    }

    private void deleteGameMessages(long chatId) {
        var messageIds = messagesToDelete.remove(chatId);
        if (messageIds == null) return;

        for (var messageId : messageIds)
            telegram.deleteMessage(chatId, messageId);
    }

    public void processWin(Message message, BncResult result) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var userStats = usersRepo.findById(userId).orElse(new Userstats(userId));
        userStats.increaseBncScore(result.getNumber().length());
        usersRepo.save(userStats);

        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);

        var username = Html.htmlSafe(message.getFrom().getFirstName());
        var text = username + " выиграл за " +
                (BncGame.totalAttempts(gameState.getLength(), gameState.isHexadecimal()) - result.getAttempts()) +
                " попыток!\n\n" + formatGameEndMessage(gameState);
        deleteGameMessages(chatId);
        telegram.sendMessage(chatId, text);
    }

    public void processGameOver(Message message, String answer) {
        var chatId = message.getChatId();
        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);
        deleteGameMessages(chatId);
        var text = "Вы проиграли! Ответ: " + answer + "\n\n" + formatGameEndMessage(gameState);
        telegram.sendMessage(chatId, text);
    }

    private String formatGameEndMessage(BncGameState state) {
        return formatHistory(state.getHistory()) +
                "\n\nПотрачено времени: " +
                formatTimeSpent((System.currentTimeMillis() - state.getStartTime()) / 1000);
    }

    private String formatHistory(List<BncResult> history) {
        return history.stream()
                .map(e -> String.format("%s: %dБ %dК", e.getNumber(), e.getBulls(), e.getCows()))
                .collect(Collectors.joining("\n"));
    }

    private String formatTimeSpent(long timeSpent) {
        var sec = timeSpent;
        var mins = sec / 60;
        sec -= mins * 60;
        var hours = mins / 60;
        mins -= hours * 60;
        return String.format("%02d:%02d:%02d", hours, mins, sec);
    }

    private String formatResult(BncResult result) {
        return String.format("%s: %dБ %dК, попыток: %d", result.getNumber(),
                result.getBulls(),
                result.getCows(),
                result.getAttempts());
    }
}
