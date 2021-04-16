package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.bnc.BncGameState;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BncStart implements CommandExecutor {

    private final BncTelegramHandler gamesHandler;

    public BncStart(BncTelegramHandler gamesHandler) {
        this.gamesHandler = gamesHandler;
    }

    @Override
    public String command() {
        return "/bnc";
    }

    @Override
    public String getDescription() {
        return "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа. " +
               " А еще можно выбрать режим игры с hexadecimal системой. /bnc hex либо /bnc hex длина. " +
               "Максимальная длина для hex - 16";
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
            int lengthIndex = isHexadecimal ? 1 : 0; // /bnc 5 or /bnc hex 5
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

        gamesHandler.createGameIfNotExists(ctx.chatId(), length, isHexadecimal);
        gamesHandler.sendGameMessage(ctx.chatId(), startText(length), ctx.sender);

    }

    private void sendGameState(MessageContext ctx, BncGameState state) {
        var historyText = state.getHistory().stream()
                .map(e -> String.format("%s: %dБ %dК", e.getNumber(), e.getBulls(), e.getCows()))
                .collect(Collectors.joining("\n"));

        var textToSend = String.format("""
                        Длина числа: %d
                        Тип числа: %s
                        Осталось попыток: %d

                        %s""",
                state.getLength(),
                state.isHexadecimal() ? "HEX" : "DEC",
                state.getAttemptsLeft(),
                historyText);

        gamesHandler.sendGameMessage(ctx.chatId(), textToSend, ctx.sender);
    }

    private String startText(int length) {
        return "Число загадано!\n" +
               "Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел!\n" +
               "Правила игры - /bnchelp.\n" +
               //"Остановить игру (голосование) - /bncstop)\n" +
               "Длина числа - " + length;
    }

    private void wrongLength(MessageContext ctx, int maxLength) {
        ctx.replyToMessage("Неверная длина числа. Допустимое значение от 4 до " + maxLength + "!").callAsync(ctx.sender);
    }
}
