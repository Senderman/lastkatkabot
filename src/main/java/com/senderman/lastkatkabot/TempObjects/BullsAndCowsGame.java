package com.senderman.lastkatkabot.TempObjects;

import com.annimon.tgbotsmodule.api.methods.Methods;
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
    private final String locale;
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
        locale = Services.i18n().getLocale(message);
        attempts = (int) (this.length * 2.5);
        voted = 0;
        antiRuinEnabled = false;
        history = new StringBuilder();
        messagesToDelete = new HashSet<>();
        antiRuinSet = new HashSet<>();
        votedUsers = new HashSet<>();
        checkedNumbers = new HashSet<>();
        messagesToDelete.add(Services.handler().sendMessage(chatId, Services.i18n().getString("generatingNumber", locale)).getMessageId());
        answer = generateRandom();
        startTime = new Date().getTime();
        Services.db().saveBncGame(chatId, this);
        messagesToDelete.add(Services.handler().sendMessage(chatId, Services.i18n().getString("bncStart", locale)).getMessageId());
    }

    public void check(Message message) {
        var number = message.getText();
        if (number.length() != length)
            return;

        messagesToDelete.add(message.getMessageId());

        if (number.equals(answer)) { // win
            history.insert(0,
                    String.format(Services.i18n().getString("bncWin", locale) + "\n\n",
                            message.getFrom().getFirstName(), (int) (length * 2.5 - (attempts - 1)), answer));
            history.append("\n")
                    .append(String.format(Services.i18n().getString("timeSpent", locale), getSpentTime()));
            Services.handler().sendMessage(chatId, history.toString());
            Services.db().incBNCWins(message.getFrom().getId(), length);
            endGame();
            return;

        }

        if (hasRepeatingDigits(number)) {
            messagesToDelete.add(
                    Services.handler().sendMessage(chatId, Services.i18n().getString("noRepeatingDigits", locale))
                            .getMessageId());
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
                messagesToDelete.add(Services.handler().sendMessage(chatId,
                        Services.i18n().getString("bncRuin", locale)).getMessageId());
                if (antiRuinEnabled)
                    return;
            }
        }

        var results = calculate(number);

        if (checkedNumbers.contains(number)) {
            messagesToDelete.add(
                    Services.handler().sendMessage(chatId, String.format(Services.i18n().getString("checkedAlready", locale),
                            number, results[0], results[1])).getMessageId());
            return;
        }

        attempts--;
        if (results[0] + results[1] == length) {
            for (int i = 0; i < length; i++) {
                antiRuinSet.add(number.charAt(i));
            }
        }
        history.append(String.format(Services.i18n().getString("bncHistory", locale) + "\n",
                message.getFrom().getFirstName(), number, results[0], results[1]));

        if (attempts > 0) {
            messagesToDelete.add(Services.handler().sendMessage(chatId,
                    String.format(Services.i18n().getString("bncCheck", locale) + "\n",
                            number, results[0], results[1], attempts))
                    .getMessageId());
            checkedNumbers.add(number);
            Services.db().saveBncGame(chatId, this);
        } else { // lose
            gameOver();
        }
    }

    public void sendGameInfo(Message message) {
        messagesToDelete.add(message.getMessageId());
        var info = String.format(Services.i18n().getString("bncInfo", locale), length, attempts, history.toString());
        messagesToDelete.add(Services.handler().sendMessage(chatId, info).getMessageId());
    }

    public void changeAntiRuin() {
        antiRuinEnabled = !antiRuinEnabled;
        String key = antiRuinEnabled ? "bncRuinOn" : "bncRuinOff";
        messagesToDelete.add(
                Services.handler().sendMessage(chatId, Services.i18n().getString(key, locale)).getMessageId());
    }

    public void createPoll(Message message) {
        messagesToDelete.add(message.getMessageId());
        if (message.isUserMessage()) { // who needs to vote in pm? :)
            gameOver();
            return;
        }

        messagesToDelete.add(Methods.sendMessage()
                .setChatId(chatId)
                .setText(String.format(Services.i18n().getString("bncVote", locale), 5 - voted))
                .setReplyMarkup(getEndgameMarkup())
                .setParseMode(ParseMode.HTML)
                .call(Services.handler()).getMessageId());
    }

    public void addVote(CallbackQuery query) {
        if (votedUsers.contains(query.getFrom().getId())) {
            Methods.answerCallbackQuery()
                    .setText(Services.i18n().getString("votedAlready", locale))
                    .setShowAlert(true)
                    .setCallbackQueryId(query.getId())
                    .call(Services.handler());
            return;
        }

        var admins = Methods.getChatAdministrators(chatId).call(Services.handler());
        var adminsIds = new HashSet<Integer>();
        for (var admin : admins) {
            adminsIds.add(admin.getUser().getId());
        }
        voted++;
        if (voted == 5 || adminsIds.contains(query.getFrom().getId())) {
            gameOver();
        }

        votedUsers.add(query.getFrom().getId());
        Methods.editMessageText()
                .setText(String.format(Services.i18n().getString("bncVote", locale), 5 - voted))
                .setChatId(chatId)
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(getEndgameMarkup())
                .setParseMode(ParseMode.HTML)
                .call(Services.handler());
    }

    private InlineKeyboardMarkup getEndgameMarkup() {
        var markup = new InlineKeyboardMarkup();
        var rows = List.of(List.of(
                new InlineKeyboardButton()
                        .setText(Services.i18n().getString("vote", locale))
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
                String.format(Services.i18n().getString("bncLose", locale) + "\n\n", answer));
        history.append("\n")
                .append(String.format(Services.i18n().getString("timeSpent", locale), getSpentTime()));
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
        for (int i = 0; i < genIndex + 1; i++) {
            for (int j = i + 1; j < genIndex + 1; j++) {
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