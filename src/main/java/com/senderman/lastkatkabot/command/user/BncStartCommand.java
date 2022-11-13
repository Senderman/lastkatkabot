package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.bnc.BncGameState;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.command.CommandExecutor;

import java.util.stream.Collectors;

@Command(
        command = "/bnc",
        description = "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа. " +
                " А еще можно выбрать режим игры с hexadecimal системой. /bnc hex либо /bnc hex длина. " +
                "Максимальная длина для hex - 16"
)
public class BncStartCommand extends CommandExecutor {

    private final BncTelegramHandler gamesHandler;

    public BncStartCommand(BncTelegramHandler gamesHandler) {
        this.gamesHandler = gamesHandler;
    }

    @Override
    public void accept(MessageContext ctx) {
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
            ctx.replyToMessage("Ошибка. Длина должна быть числом").callAsync(ctx.sender);
            return;
        }

        gamesHandler.createGameIfNotExists(ctx.chatId(), ctx.message().getFrom().getId(), length, isHexadecimal);
        gamesHandler.sendGameMessage(ctx.chatId(), startText(length), ctx.sender);

    }

    private void sendGameState(MessageContext ctx, BncGameState state) {
        var historyText = state.history().stream()
                .map(e -> String.format("%s: %dБ %dК", e.number(), e.bulls(), e.cows()))
                .collect(Collectors.joining("\n"));

        var textToSend = String.format("""
                        Длина числа: %d
                        Тип числа: %s
                        Осталось попыток: %d

                        %s""",
                state.length(),
                state.isHexadecimal() ? "HEX" : "DEC",
                state.attemptsLeft(),
                historyText);

        gamesHandler.sendGameMessage(ctx.chatId(), textToSend, ctx.sender);
    }

    private String startText(int length) {
        return """
                Число загадано!
                Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел!
                Правила игры - /bnchelp
                Статус текущей игры - /bnc
                Остановить игру - /bncstop
                Длина числа - %d
                """.formatted(length);
    }

    private void wrongLength(MessageContext ctx, int maxLength) {
        ctx.replyToMessage("Неверная длина числа. Допустимое значение от 4 до " + maxLength + "!").callAsync(ctx.sender);
    }
}
