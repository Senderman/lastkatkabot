package com.senderman.lastkatkabot.feature.bnc.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Command
public class BncStopCommand implements CommandExecutor {

    private final BncTelegramHandler gameHandler;
    private final long period = TimeUnit.HOURS.toMillis(1);

    public BncStopCommand(BncTelegramHandler bncTelegramHandler) {
        this.gameHandler = bncTelegramHandler;
    }

    @Override
    public String command() {
        return "/bncstop";
    }

    @Override
    public String getDescription() {
        return "bnc.bncstop.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        long chatId = ctx.chatId();
        if (!gameHandler.hasGame(chatId)) {
            ctx.replyToMessage(ctx.getString("bnc.bncstop.noGame")).callAsync(ctx.sender);
            return;
        }

        var gameState = gameHandler.getGameState(chatId);
        var startTime = gameState.startTime();

        boolean isCreator = ctx.message().getFrom().getId().equals(gameState.creatorId());
        boolean isOneHourPassed = System.currentTimeMillis() - startTime > period;

        if (!isCreator && !isOneHourPassed) {
            ctx.replyToMessage(ctx.getString("bnc.bncstop.stopRequirements"))
                    .callAsync(ctx.sender);
            return;
        }

        gameHandler.forceFinishGame(ctx, chatId);
    }
}
