package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.bnc.BncGameState;
import com.senderman.lastkatkabot.bnc.BncTelegramHandler;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class BncStart implements CommandExecutor {

    private final BncTelegramHandler gamesHandler;
    private final CommonAbsSender telegram;

    public BncStart(BncTelegramHandler gamesHandler, CommonAbsSender telegram) {
        this.gamesHandler = gamesHandler;
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/bnc";
    }

    @Override
    public String getDescription() {
        return "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа. " +
                " А еще можно выбрать режим игры с hexadecimal системой. /bnc hex либо /bnc hex длина. " +
                "Максимальная длина для hex - 16";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        int length;
        boolean isHexadecimal = false;
        var args = message.getText().split("\\s+");
        if (args.length > 1 && args[1].equalsIgnoreCase("hex"))
            isHexadecimal = true;
        try {
            length = parseLength(isHexadecimal, args);
            int maxLength = isHexadecimal ? 16 : 10;
            if (length < 4 || length > maxLength) {
                wrongLength(chatId, maxLength);
                return;
            }
        } catch (NumberFormatException e) {
            Methods.sendMessage(chatId, "Ошибка. Длина должна быть числом").callAsync(telegram);
            return;
        }


        // if there's game in this chat already, send state
        try {
            var gameState = gamesHandler.getGameState(chatId);
            sendGameState(chatId, gameState);
        } catch (NoSuchElementException e) {
            gamesHandler.createGameIfNotExists(chatId, length, isHexadecimal);
            gamesHandler.sendGameMessage(chatId, startText(length));
        }

    }


    private int parseLength(boolean isHexadecimal, String[] args) throws NumberFormatException {
        if (!isHexadecimal) {
            if (args.length == 1) return 4;
            return Integer.parseInt(args[1]);
        } else {
            if (args.length == 2) return 4;
            return Integer.parseInt(args[2]);
        }
    }

    private void sendGameState(long chatId, BncGameState state) {
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

        gamesHandler.sendGameMessage(chatId, textToSend);
    }

    private String startText(int length) {
        return "Число загадано!\n" +
                "Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел!\n" +
                "Правила игры - /bnchelp.\n" +
                "Остановить игру (голосование) - /bncstop)\n" +
                "Длина числа - " + length;
    }

    private void wrongLength(long chatId, int maxLength) {
        Methods.sendMessage(chatId, "Неверная длина числа. Допустимое значение от 4 до " + maxLength + "!")
                .callAsync(telegram);
    }
}
