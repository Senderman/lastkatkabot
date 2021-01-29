package com.senderman.lastkatkabot.command.user;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.bnc.BncDatabaseController;
import com.senderman.lastkatkabot.bnc.BncGameState;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class BncStart implements CommandExecutor {

    private final BncDatabaseController gamesController;
    private final ApiRequests telegram;

    public BncStart(BncDatabaseController gamesController, ApiRequests telegram) {
        this.gamesController = gamesController;
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/bnc";
    }

    @Override
    public String getDescription() {
        return "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        int length;
        var args = message.getText().split("\\s+", 2);
        if (args.length == 1) {
            length = 4;
        } else {
            try {
                length = Integer.parseInt(args[1]);
                if (length < 4 || length > 10) {
                    wrongLength(chatId);
                    return;
                }
            } catch (NumberFormatException e) {
                wrongLength(chatId);
                return;
            }
        }

        // if there's game in this chat already, send state
        try {
            var gameState = gamesController.getGameState(chatId);
            sendGameState(chatId, gameState);
        } catch (NoSuchElementException e) {
            gamesController.createGameIfNotExists(chatId, length);
            telegram.sendMessage(chatId, startText(length));
        }

    }

    private void sendGameState(long chatId, BncGameState state) {
        var historyText = state.getHistory().stream()
                .map(e -> String.format("%s: %dБ %dК", e.getNumber(), e.getBulls(), e.getCows()))
                .collect(Collectors.joining("\n"));

        var textToSend = String.format("Длина числа: %d\n" +
                "Осталось попыток: %d\n\n" +
                "%s", state.getLength(), state.getAttemptsLeft(), historyText);

        telegram.sendMessage(chatId, textToSend);
    }

    private String startText(int length) {
        return "Число загадано!\n" +
                "Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел!\n" +
                "Правила игры - /bnchelp.\n" +
                "Остановить игру (голосование) - /bncstop)\n" +
                "Длина числа - " + length;
    }

    private void wrongLength(long chatId) {
        telegram.sendMessage(chatId, "Неверная длина числа. Допустимое значение от 4 до 10!");
    }
}
