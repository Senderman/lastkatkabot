package com.senderman.lastkatkabot.bnc;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.bnc.exception.*;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BncTelegramHandler implements RegexCommand {

    private final Pattern pattern = Pattern.compile("(\\d|[a-fA-F]){4,16}");
    private final BncGamesManager gamesManager;
    private final UserStatsService usersRepo;
    private final Map<Long, List<Integer>> messagesToDelete;

    public BncTelegramHandler(
            @Qualifier("bncDatabaseManager") BncGamesManager gamesManager,
            UserStatsService usersRepo
    ) {
        this.gamesManager = gamesManager;
        this.usersRepo = usersRepo;
        this.messagesToDelete = new HashMap<>();
    }

    @Override
    public Pattern pattern() {
        return pattern;
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.USER);
    }

    public void accept(RegexMessageContext ctx) {
        var message = ctx.message();
        var telegram = ctx.sender;
        var chatId = ctx.chatId();
        var number = message.getText().toUpperCase(Locale.ENGLISH);
        try {
            var result = gamesManager.check(chatId, number);
            addMessageToDelete(message);
            if (result.isWin()) {
                processWin(message, result, telegram);
            } else {
                sendGameMessage(chatId, formatResult(result), telegram);
            }
        } catch (NumberAlreadyCheckedException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Уже проверяли! " + formatResult(e.getResult()), telegram);
        } catch (RepeatingDigitsException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Число не должно иметь повторяющихся цифр!", telegram);
        } catch (GameOverException e) {
            addMessageToDelete(message);
            processGameOver(message, e.getAnswer(), telegram);
        } catch (InvalidLengthException | InvalidCharacterException | NoSuchElementException ignored) {
        }
    }

    public BncGameState getGameState(long chatId) {
        return gamesManager.getGameState(chatId);
    }

    public boolean hasGame(long chatId) {
        return gamesManager.hasGame(chatId);
    }

    public boolean createGameIfNotExists(long chatId, int length, boolean isHexadecimal) {
        return gamesManager.createGameIfNotExists(chatId, length, isHexadecimal);
    }

    // Send message that will be deleted after game end
    public void sendGameMessage(long chatId, String text, CommonAbsSender telegram) {
        var sentMessage = Methods.sendMessage(chatId, text).call(telegram);
        if (sentMessage != null)
            addMessageToDelete(sentMessage);
    }

    private void addMessageToDelete(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var list = messagesToDelete.computeIfAbsent(chatId, k -> new ArrayList<>());
        list.add(messageId);
    }

    private void deleteGameMessages(long chatId, CommonAbsSender telegram) {
        var messageIds = messagesToDelete.remove(chatId);
        if (messageIds == null) return;

        for (var messageId : messageIds)
            Methods.deleteMessage(chatId, messageId).callAsync(telegram);
    }

    public void processWin(Message message, BncResult result, CommonAbsSender telegram) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var userStats = usersRepo.findById(userId);
        userStats.increaseBncScore(result.getNumber().length());
        usersRepo.save(userStats);

        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);

        var username = Html.htmlSafe(message.getFrom().getFirstName());
        var text = username + " выиграл за " +
                   (BncGame.totalAttempts(gameState.getLength(), gameState.isHexadecimal()) - result.getAttempts()) +
                   " попыток!\n\n" + formatGameEndMessage(gameState);
        deleteGameMessages(chatId, telegram);
        Methods.sendMessage(chatId, text).callAsync(telegram);
    }

    public void processGameOver(Message message, String answer, CommonAbsSender telegram) {
        var chatId = message.getChatId();
        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);
        deleteGameMessages(chatId, telegram);
        var text = "Вы проиграли! Ответ: " + answer + "\n\n" + formatGameEndMessage(gameState);
        Methods.sendMessage(chatId, text).callAsync(telegram);
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
