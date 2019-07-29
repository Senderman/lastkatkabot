package com.senderman.lastkatkabot.tempobjects;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BullsAndCowsGame {
    private final long chatId;
    private final long startTime;
    private final String answer;
    private final StringBuilder history;
    private final Set<Integer> messagesToDelete;
    private final Set<Character> antiRuinSet;
    private final Set<Integer> votedUsers;
    private final Set<String> checkedNumbers;
    private int length;
    private int attempts;
    private int voted;
    private boolean antiRuinEnabled;

    public BullsAndCowsGame(Message message) {
        this.chatId = message.getChatId();
        int length;
        try {
            length = Integer.parseInt(message.getText().split(" ")[1]);
            if (length < 4 || length > 10)
                this.length = 4;
            else
                this.length = length;
        } catch (Exception e) {
            this.length = 4;
        }
        attempts = (int) (this.length * 2.5);
        voted = 0;
        antiRuinEnabled = false;
        history = new StringBuilder();
        messagesToDelete = new HashSet<>();
        antiRuinSet = new HashSet<>();
        votedUsers = new HashSet<>();
        checkedNumbers = new HashSet<>();
        gameMessage(chatId, "Генерируем число...");
        answer = generateRandom();
        startTime = new Date().getTime();
        Services.db().saveBncGame(chatId, this);
        gameMessage(chatId, "Число загадано!\n" +
                "Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел!\n" +
                "Правила игры - /bnchelp.\n" +
                "Вкл/выкл режима антируина (когда все цифры известны) - /bncruin\n" +
                "Просмотр хода игры - /bncinfo\n" +
                "Остановить игру (голосование) - /bncstop");
    }

    public void check(Message message) {
        var number = message.getText();
        if (number.length() != length)
            return;

        messagesToDelete.add(message.getMessageId());

        if (number.equals(answer)) { // win
            history.insert(0,
                    String.format("%1$s выиграл за %2$d попыток! %3$s - правильный ответ!\n\n",
                            message.getFrom().getFirstName(), (int) (length * 2.5 - (attempts - 1)), answer));
            history.append("\n")
                    .append(String.format("Вот столько вы потратили времени: %1$s", getSpentTime()));
            Services.handler().sendMessage(chatId, history.toString());
            Services.db().incBNCWins(message.getFrom().getId(), length);
            endGame();
            return;

        }

        if (hasRepeatingDigits(number)) {
            gameMessage(chatId, "Загаданное число не может содержать повторяющиеся числа!");
            return;
        }

        // when all digits are known
        if (antiRuinSet.size() != 0) {
            var ruined = false;
            for (int i = 0; i < length; i++) {
                if (!antiRuinSet.contains(number.charAt(i))) {
                    ruined = true;
                    break;
                }
            }
            if (ruined) {
                gameMessage(chatId, "Все правильные цифры уже известны, следите за игрой!");
                if (antiRuinEnabled)
                    return;
            }
        }

        var results = calculate(number);

        if (checkedNumbers.contains(number)) {
            gameMessage(chatId, String.format("%1$s - уже проверяли! %2$dБ %3$dК",
                    number, results[0], results[1]));
            return;
        }

        attempts--;
        if (results[0] + results[1] == length) {
            for (int i = 0; i < length; i++) {
                antiRuinSet.add(number.charAt(i));
            }
        }
        history.append(String.format("%1$s - %2$s: %3$dБ %4$dК\n",
                message.getFrom().getFirstName(), number, results[0], results[1]));

        if (attempts > 0) {
            gameMessage(chatId,
                    String.format("%1$s: %2$dБ %3$dК, попыток: %4$d\n",
                            number, results[0], results[1], attempts));
            checkedNumbers.add(number);
            Services.db().saveBncGame(chatId, this);
        } else { // lose
            gameOver();
        }
    }

    public void sendGameInfo(Message message) {
        messagesToDelete.add(message.getMessageId());
        var info = String.format("Длина числа: %1$d\n" +
                "Попыток осталось: %2$d\n" +
                "История:\n" +
                "%3$s", length, attempts, history.toString());
        gameMessage(chatId, info);
    }

    public void changeAntiRuin() {
        antiRuinEnabled = !antiRuinEnabled;
        String status = antiRuinEnabled ? "Антируин включен!" : "Антируин выключен!";
        gameMessage(chatId, status);
    }

    public void createStopPoll(Message message) {
        messagesToDelete.add(message.getMessageId());
        if (message.isUserMessage()) { // who needs to vote in pm? :)
            gameOver();
            return;
        }

        gameMessage(Methods.sendMessage()
                .setChatId(chatId)
                .setText(String.format("<b>Голосование за завершение игры</b>\n" +
                        "Осталось %1$d голосов для завершения. Голос админа чата сразу заканчивает игру",
                        5 - voted))
                .setReplyMarkup(getEndgameMarkup())
                .setParseMode(ParseMode.HTML));
    }

    public void addVote(CallbackQuery query) {
        if (votedUsers.contains(query.getFrom().getId())) {
            Methods.answerCallbackQuery()
                    .setText("Вы уже голосовали!")
                    .setShowAlert(true)
                    .setCallbackQueryId(query.getId())
                    .call(Services.handler());
            return;
        }

        voted++;
        var user = Methods.getChatMember(chatId, query.getFrom().getId()).call(Services.handler());
        if (voted == 5 || user.getStatus().equals("creator") || user.getStatus().equals("administrator")) {
            gameOver();
        }

        votedUsers.add(query.getFrom().getId());
        Methods.editMessageText()
                .setText(String.format("<b>Голосование за завершение игры</b>\n" +
                        "Осталось %1$d голосов для завершения. Голос админа чата сразу заканчивает игру",
                        5 - voted))
                .setChatId(chatId)
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(getEndgameMarkup())
                .setParseMode(ParseMode.HTML)
                .call(Services.handler());
    }

    private void gameMessage(long chatId, String text) {
        gameMessage(Methods.sendMessage(chatId, text));
    }

    private void gameMessage(SendMessageMethod sm) {
        messagesToDelete.add(Services.handler().sendMessage(sm).getMessageId());
    }

    private static InlineKeyboardMarkup getEndgameMarkup() {
        var markup = new InlineKeyboardMarkup();
        var rows = List.of(List.of(
                new InlineKeyboardButton()
                        .setText("Голосовать")
                        .setCallbackData(LastkatkaBot.CALLBACK_VOTE_BNC)));
        markup.setKeyboard(rows);
        return markup;
    }

    private void endGame() {
        for (int messageId : messagesToDelete) {
            Methods.deleteMessage(chatId, messageId).call(Services.handler());
        }
        Services.db().deleteBncGame(chatId);
        Services.handler().bullsAndCowsGames.remove(chatId);
    }

    private void gameOver() {
        history.insert(0,
                String.format("Вы проиграли! Ответ: %1$s\n\n", answer));
        history.append("\n")
                .append(String.format("Вот столько вы потратили времени: %1$s", getSpentTime()));
        Services.handler().sendMessage(chatId, history.toString());
        endGame();
    }

    private String getSpentTime() {
        var endTime = new Date().getTime();
        var timeSpent = endTime - startTime;

        long hr = TimeUnit.MILLISECONDS.toHours(timeSpent);
        long min = TimeUnit.MILLISECONDS.toMinutes(timeSpent - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(timeSpent - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        return String.format("%02d:%02d:%02d", hr, min, sec);

    }

    private String generateRandom() {
        var random = new int[length];
        for (int i = 0; i < length; i++) {
            do {
                random[i] = ThreadLocalRandom.current().nextInt(0, 10);
            } while (hasRepeatingDigits(random, i));
        }
        var sb = new StringBuilder();
        for (var i : random)
            sb.append(i);
        return sb.toString();
    }

    // check that array contains only unique numbers
    private boolean hasRepeatingDigits(String array) {
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                if (array.charAt(i) == array.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }

    // same as before but for an array under generating
    private boolean hasRepeatingDigits(int[] array, int genIndex) {
        for (int i = 0; i <= genIndex; i++) {
            for (int j = i + 1; j <= genIndex; j++) {
                if (array[i] == array[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    //calculate bulls and cows
    private int[] calculate(String player) {
        int bulls = 0, cows = 0;
        for (int i = 0; i < length; i++) {
            if (player.charAt(i) == answer.charAt(i)) {
                bulls++;
            } else {
                for (int j = 0; j < length; j++) {
                    if (player.charAt(i) == answer.charAt(j)) {
                        cows++;
                    }
                }
            }
        }
        return new int[]{bulls, cows};
    }
}