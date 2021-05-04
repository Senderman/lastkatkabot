package com.senderman.lastkatkabot.bnc;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.bnc.exception.*;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO replace sender with context
@Component
public class BncTelegramHandler implements RegexCommand {

    private final Pattern pattern = Pattern.compile("(\\d|[a-fA-F]){4,16}");
    private final BncGamesManager gamesManager;
    private final UserStatsService usersRepo;

    public BncTelegramHandler(
            @Qualifier("bncDatabaseManager") BncGamesManager gamesManager,
            UserStatsService usersRepo
    ) {
        this.gamesManager = gamesManager;
        this.usersRepo = usersRepo;
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
        long chatId = ctx.chatId();
        var number = message.getText().toUpperCase(Locale.ENGLISH);
        try {
            var result = gamesManager.check(chatId, number);
            addMessageToDelete(message);
            if (result.isWin()) {
                processWin(ctx, result);
            } else {
                sendGameMessage(ctx.chatId(), formatResult(result), ctx.sender);
            }
        } catch (NumberAlreadyCheckedException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Уже проверяли! " + formatResult(e.getResult()), telegram);
        } catch (RepeatingDigitsException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, "Число не должно иметь повторяющихся цифр!", telegram);
        } catch (GameOverException e) {
            addMessageToDelete(message);
            processGameOver(ctx, e.getAnswer());
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
    public void sendGameMessage(long chatId, String text, CommonAbsSender sender) {
        var sentMessage = Methods.sendMessage(chatId, text).call(sender);
        if (sentMessage != null)
            addMessageToDelete(sentMessage);
    }

    private void addMessageToDelete(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        gamesManager.addFloodMessage(messageId, chatId);
    }

    private void deleteGameMessages(long chatId, CommonAbsSender telegram) {
        var messages = gamesManager.getFloodMessagesByGameId(chatId);
        if (messages == null) return;

        FloodDeleteExceptionConsumer exceptionsConsumer = new FloodDeleteExceptionConsumer(
                telegram, chatId
        );

        for (var message : messages) {
            int messageId = message.getMessageId();
            Methods.deleteMessage(chatId, messageId).callAsync(
                    telegram, null, exceptionsConsumer::onException, null);
        }
        gamesManager.deleteFloodMessagesByGameId(chatId);
    }

    public void processWin(MessageContext ctx, BncResult result) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var userStats = usersRepo.findById(userId);
        userStats.increaseBncScore(result.getNumber().length());
        usersRepo.save(userStats);

        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);

        var username = Html.htmlSafe(ctx.user().getFirstName());
        var text = username + " выиграл за " +
                   (BncGame.totalAttempts(gameState.getLength(), gameState.isHexadecimal()) - result.getAttempts()) +
                   " попыток!\n\n" + formatGameEndMessage(gameState);
        deleteGameMessages(chatId, ctx.sender);
        ctx.reply(text).callAsync(ctx.sender);
    }

    public void processGameOver(MessageContext ctx, String answer) {
        long chatId = ctx.chatId();
        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);
        deleteGameMessages(chatId, ctx.sender);
        var text = "Вы проиграли! Ответ: " + answer + "\n\n" + formatGameEndMessage(gameState);
        ctx.reply(text).callAsync(ctx.sender);
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

    static private class FloodDeleteExceptionConsumer {
        private int counter;
        private final CommonAbsSender telegram;
        private final long chatId;

        public FloodDeleteExceptionConsumer(CommonAbsSender telegram, long chatId) {
            this.counter = 0;
            this.telegram = telegram;
            this.chatId = chatId;
        }

        public void onException(TelegramApiException e){
            if (counter > 0) {
                return;
            }
            String message = e.getMessage();
            if (message.equals("Error deleting message")) {
                Methods.sendMessage(
                        chatId,
                        "Чтобы я удалял ВСЕ сообщения в bnc, выдайте мне права на удаление сообщений в чате!"
                ).callAsync(telegram);
            }
            this.counter = 1;
        }
    }
}
