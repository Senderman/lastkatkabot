package com.senderman.lastkatkabot.feature.bnc.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.feature.bnc.BncGame;
import com.senderman.lastkatkabot.feature.bnc.exception.*;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameMessage;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.bnc.model.BncRecord;
import com.senderman.lastkatkabot.feature.bnc.model.BncResult;
import com.senderman.lastkatkabot.feature.bnc.service.BncGameMessageService;
import com.senderman.lastkatkabot.feature.bnc.service.BncGamesManager;
import com.senderman.lastkatkabot.feature.bnc.service.BncRecordService;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TimeUtils;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class BncTelegramHandler implements RegexCommand {

    private final Pattern pattern = Pattern.compile("(\\d|[a-fA-F]){4,16}");
    private final BncGamesManager gamesManager;
    private final UserStatsService usersRepo;
    private final BncGameMessageService gameMessageRepo;
    private final BncRecordService recordRepo;
    private final L10nService localizationService;
    private final TimeUtils timeUtils;

    public BncTelegramHandler(
            @Named("bncDatabaseManager") BncGamesManager gamesManager,
            UserStatsService usersRepo,
            BncGameMessageService gameMessageRepo,
            BncRecordService recordRepo,
            L10nService localizationService,
            TimeUtils timeUtils
    ) {
        this.gamesManager = gamesManager;
        this.usersRepo = usersRepo;
        this.gameMessageRepo = gameMessageRepo;
        this.recordRepo = recordRepo;
        this.localizationService = localizationService;
        this.timeUtils = timeUtils;
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
        var l10Ctx = new L10nMessageContext(ctx.sender, ctx.update(), ctx.argumentsAsString(), localizationService);
        var message = ctx.message();
        var telegram = ctx.sender;
        long chatId = ctx.chatId();
        var number = message.getText().toUpperCase(Locale.ENGLISH);
        try {
            var result = gamesManager.check(chatId, number);
            addMessageToDelete(message);
            if (result.isWin()) {
                processWin(gamesManager.getGameState(chatId), l10Ctx, result);
            } else {
                sendGameMessage(ctx.chatId(), formatResult(result, l10Ctx), ctx.sender);
            }
        } catch (NumberAlreadyCheckedException e) {
            addMessageToDelete(message);
            sendGameMessage(
                    chatId,
                    l10Ctx.getString("bnc.handler.alreadyChecked").formatted(formatResult(e.getResult(), l10Ctx)),
                    telegram
            );
        } catch (RepeatingDigitsException e) {
            addMessageToDelete(message);
            sendGameMessage(chatId, l10Ctx.getString("bnc.handler.noRepeatingDigits"), telegram);
        } catch (GameOverException e) {
            addMessageToDelete(message);
            processGameOver(l10Ctx);
        } catch (InvalidLengthException | InvalidCharacterException | NoSuchElementException ignored) {
        }
    }

    public BncGameState getGameState(long chatId) {
        return gamesManager.getGameState(chatId);
    }

    public boolean hasGame(long chatId) {
        return gamesManager.hasGame(chatId);
    }

    public boolean createGameIfNotExists(long chatId, long userId, int length, boolean isHexadecimal) {
        return gamesManager.createGameIfNotExists(chatId, userId, length, isHexadecimal);
    }

    // Send message that will be deleted after game end
    public void sendGameMessage(long chatId, String text, CommonAbsSender sender) {
        var sentMessage = Methods.sendMessage(chatId, text).call(sender);
        if (sentMessage != null)
            addMessageToDelete(sentMessage);
    }

    private void addMessageToDelete(@NonNull Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        gameMessageRepo.save(new BncGameMessage(chatId, messageId));
    }

    private void deleteGameMessages(long chatId, CommonAbsSender telegram) {
        var gameMessages = gameMessageRepo.findByGameId(chatId);
        if (gameMessages.isEmpty()) return;
        var messagesToDelete = gameMessages.stream()
                .map(BncGameMessage::getMessageId)
                .toList();
        var m = Methods.deleteMessages(chatId);
        m.setMessageIds(messagesToDelete);
        m.callAsync(telegram);
    }

    public void processWin(BncGameState game, L10nMessageContext ctx, BncResult result) {
        long chatId = ctx.chatId();
        var userId = ctx.user().getId();
        var userStats = usersRepo.findById(userId);
        int score = game.isHexadecimal() ? (int) (game.length() * 1.5) : game.length();
        userStats.increaseBncScore(score);
        usersRepo.save(userStats);

        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);

        var username = Html.htmlSafe(ctx.user().getFirstName());
        long timeSpent = getTimeSpent(gameState);
        var text = ctx.getString("bnc.handler.userWon").formatted(
                username,
                BncGame.totalAttempts(gameState.length(), gameState.isHexadecimal()) - result.attempts(),
                score,
                formattedHistoryAndTime(gameState.history(), timeSpent, ctx)
        );
        deleteGameMessages(chatId, ctx.sender);
        ctx.reply(text).callAsync(ctx.sender);

        var previousRecordOptional = recordRepo.findByLengthAndHexadecimal(gameState.length(), gameState.isHexadecimal());
        // if there wasn't any record before
        if (previousRecordOptional.isEmpty()) {
            var newRecord = new BncRecord(gameState.length(), gameState.isHexadecimal());
            newRecord.setTimeSpent(timeSpent);
            newRecord.setUserId(userId);
            newRecord.setName(username);
            recordRepo.save(newRecord);
            var recordText = ctx.getString("bnc.handler.firstRecord").formatted(timeUtils.formatTimeSpent(timeSpent));
            ctx.reply(recordText).callAsync(ctx.sender);
            return;
        }
        // if there's a new record taken
        var previousRecord = previousRecordOptional.get();
        long previousTime = previousRecord.getTimeSpent();
        if (timeSpent < previousTime) {
            previousRecord.setName(username);
            previousRecord.setUserId(userId);
            previousRecord.setTimeSpent(timeSpent);
            recordRepo.save(previousRecord);
            var recordText = ctx.getString("bnc.handler.newRecord").formatted(
                    timeUtils.formatTimeSpent(previousTime),
                    timeUtils.formatTimeSpent(timeSpent)
            );
            ctx.reply(recordText).callAsync(ctx.sender);
        }
    }

    public void processGameOver(L10nMessageContext ctx) {
        long chatId = ctx.chatId();
        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);
        deleteGameMessages(chatId, ctx.sender);
        var text = ctx.getString("bnc.handler.gameOver")
                .formatted(gameState.answer(), formattedHistoryAndTime(gameState.history(), getTimeSpent(gameState), ctx));
        ctx.reply(text).callAsync(ctx.sender);
    }

    public void forceFinishGame(L10nMessageContext ctx) {
        long chatId = ctx.chatId();
        if (!gamesManager.hasGame(chatId))
            return;

        var gameState = gamesManager.getGameState(chatId);
        gamesManager.deleteGame(chatId);
        deleteGameMessages(chatId, ctx.sender);

        var text = ctx.getString("bnc.handler.forceFinish")
                .formatted(gameState.answer(), formattedHistoryAndTime(gameState.history(), getTimeSpent(gameState), ctx));
        ctx.replyToMessage(text).callAsync(ctx.sender);
    }

    private String formattedHistoryAndTime(List<BncResult> history, long timeSpent, L10nMessageContext ctx) {
        return ctx.getString("bnc.handler.gameStateStats")
                .formatted(
                        formatHistory(history, ctx),
                        timeUtils.formatTimeSpent(timeSpent)
                );
    }

    private String formatHistory(List<BncResult> history, L10nMessageContext ctx) {
        return history.stream()
                .map(e -> ctx.getString("bnc.handler.historyLine").formatted(e.number(), e.bulls(), e.cows()))
                .collect(Collectors.joining("\n"));
    }

    private long getTimeSpent(BncGameState gameState) {
        return (System.currentTimeMillis() - gameState.startTime()) / 1000;
    }

    private String formatResult(BncResult result, L10nMessageContext ctx) {
        return ctx.getString("bnc.handler.result").formatted(
                result.number(),
                result.bulls(),
                result.cows(),
                result.attempts()
        );
    }
}
