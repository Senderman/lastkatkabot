package com.senderman.lastkatkabot.feature.bnc.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.bnc.model.BncGameState;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@Command
public class BncStartCommand implements CommandExecutor {

    private final BncTelegramHandler gamesHandler;

    public BncStartCommand(BncTelegramHandler gamesHandler) {
        this.gamesHandler = gamesHandler;
    }

    @Override
    public String command() {
        return "/bnc";
    }

    @Override
    public String getDescriptionKey() {
        return "bnc.bncstart.desc";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        // if there's game in this chat already, send state
        if (gamesHandler.hasGame(ctx.chatId())) {
            sendGameState(ctx, gamesHandler.getGameState(ctx.chatId()));
            return;
        }

        int length;
        boolean isHexadecimal = ctx.argument(0, "").equalsIgnoreCase("hex");
        try {
            int lengthIndex = isHexadecimal ? 1 : 0; // /bnc hex 5 or /bnc 5
            length = Integer.parseInt(ctx.argument(lengthIndex, "4"));
            int maxLength = isHexadecimal ? 16 : 10;
            if (length < 4 || length > maxLength) {
                wrongLength(ctx, maxLength);
                return;
            }
        } catch (NumberFormatException e) {
            ctx.replyToMessage(ctx.getString("bnc.bncstart.lengthIsNumber")).callAsync(ctx.sender);
            return;
        }

        gamesHandler.createGameIfNotExists(ctx.chatId(), ctx.message().getFrom().getId(), length, isHexadecimal);
        gamesHandler.sendGameMessage(ctx.getString("bnc.bncstart.startText").formatted(length), ctx);

    }

    private void sendGameState(L10nMessageContext ctx, BncGameState state) {
        var historyText = state.history().stream()
                .map(e -> "%s: %dБ %dК".formatted(e.number(), e.bulls(), e.cows()))
                .collect(Collectors.joining("\n"));

        var textToSend = ctx.getString("bnc.bncstart.gameState").formatted(
                state.length(),
                state.isHexadecimal() ? "HEX" : "DEC",
                state.attemptsLeft(),
                historyText
        );

        gamesHandler.sendGameMessage(textToSend, ctx);
    }

    private void wrongLength(L10nMessageContext ctx, int maxLength) {
        ctx.replyToMessage(ctx.getString("bnc.bncstart.wrongLength").formatted(maxLength)).callAsync(ctx.sender);
    }
}
