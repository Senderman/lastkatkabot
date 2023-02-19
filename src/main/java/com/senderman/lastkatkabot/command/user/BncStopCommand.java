package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.command.CommandExecutor;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Singleton
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
        return "остановить игру \"быки и коровы\" в текущем чате";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {
        long chatId = ctx.chatId();
        if (!gameHandler.hasGame(chatId)) {
            ctx.replyToMessage("В этом чате нет игры!").callAsync(ctx.sender);
            return;
        }

        var gameState = gameHandler.getGameState(chatId);
        var startTime = gameState.startTime();

        boolean isCreator = ctx.message().getFrom().getId().equals(gameState.creatorId());
        boolean isOneHourPassed = System.currentTimeMillis() - startTime > period;

        if (!isCreator && !isOneHourPassed) {
            ctx.replyToMessage("Для остановки игры в группе, вы должны быть создателем игры, либо " +
                            "с момента создания игры должно пройти не менее 1 часа!")
                    .callAsync(ctx.sender);
            return;
        }

        gameHandler.forceFinishGame(ctx.sender, chatId);
    }
}
