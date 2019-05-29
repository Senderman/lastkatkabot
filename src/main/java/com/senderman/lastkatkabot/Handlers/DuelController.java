package com.senderman.lastkatkabot.Handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import com.senderman.lastkatkabot.TempObjects.TgUser;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DuelController {

    private final LastkatkaBotHandler handler;
    private final Map<Long, Map<Integer, Duel>> duels;

    public DuelController(LastkatkaBotHandler handler) {
        this.handler = handler;
        duels = new ConcurrentHashMap<>();
    }

    public void createNewDuel(Message message) {
        if (message.isUserMessage())
            return;
        var chatId = message.getChatId();
        var player1 = message.getFrom();
        var sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText("\uD83C\uDFAF " + Services.i18n().getString("announceDuel", Services.i18n().getLocale(message))
                        + "\n" + player1.getFirstName());
        var duelMessageId = handler.sendMessage(sm).getMessageId();
        var duel = new Duel(chatId, duelMessageId);
        duel.player1 = new TgUser(player1.getId(), player1.getFirstName());
        getChatDuels(chatId).put(duelMessageId, duel);
        setReplyMarkup(chatId, duelMessageId);
    }

    public void joinDuel(CallbackQuery query) {
        var userLocale = Services.i18n().getLocale(query);
        var duelMessageId = query.getMessage().getMessageId();
        var chatDuels = getChatDuels(query.getMessage().getChatId());
        var player2 = new TgUser(query.getFrom().getId(), query.getFrom().getFirstName());
        if (!chatDuels.containsKey(duelMessageId)) {
            answerCallbackQuery(query, "⏰ " + Services.i18n().getString("oldDuel", userLocale), true);
            return;
        }

        var duel = chatDuels.get(duelMessageId);
        if (duel.player2 != null) {
            answerCallbackQuery(query, "\uD83D\uDEAB " + Services.i18n().getString("tooLate", userLocale), true);
            return;
        }
        if (duel.player1.getId() == player2.getId()) {
            answerCallbackQuery(query, "\uD83D\uDC7A " + Services.i18n().getString("suicide", userLocale), true);
            return;
        }

        duel.player2 = player2;
        answerCallbackQuery(query, "✅ " + Services.i18n().getString("joinedDuel", userLocale), false);
        startDuel(duel, Services.db().getChatLocale(query.getMessage().getChatId()));
        chatDuels.remove(duelMessageId);
    }

    private void startDuel(Duel duel, String locale) {
        var randomInt = ThreadLocalRandom.current().nextInt(100);
        var winner = (randomInt < 50) ? duel.player1 : duel.player2;
        var loser = (randomInt < 50) ? duel.player2 : duel.player1;

        var winnerName = winner.getName();
        var loserName = loser.getName();

        var duelResult = new StringBuilder();
        duelResult.append(String.format(
                Services.i18n().getString("duelHistory", locale) + "\n\n",
                duel.player1.getName(), duel.player2.getName(), winnerName, loserName));

        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            duelResult.append(String.format(Services.i18n().getString("duelTie", locale),
                    loserName, winnerName));
            Services.db().incTotalDuels(winner.getId());
            Services.db().incTotalDuels(loser.getId());

        } else {
            duelResult.append(String.format("\uD83D\uDC51 <b>" + Services.i18n().getString("duelWinner", locale) + "</b>", winnerName));
            Services.db().incDuelWins(winner.getId());
            Services.db().incTotalDuels(loser.getId());
        }

        Methods.editMessageText()
                .setChatId(duel.chatId)
                .setMessageId(duel.messageId)
                .setText(duelResult.toString())
                .setParseMode(ParseMode.HTML)
                .call(handler);

    }

    public void critical(Message message) {
        duels.clear();
        handler.sendMessage(message.getChatId(), "✅ " + Services.i18n().getString("duelsRemoved", message));
    }

    private void setReplyMarkup(long chatId, int duelMessageId) {
        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                .setText(Services.i18n().getString("join", Services.db().getChatLocale(chatId)))
                .setCallbackData(LastkatkaBot.CALLBACK_JOIN_DUEL));
        markup.setKeyboard(List.of(row1));
        Methods.editMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(duelMessageId)
                .setReplyMarkup(markup)
                .call(handler);
    }

    private void answerCallbackQuery(CallbackQuery query, String text, boolean showAsAlert) {
        Methods.answerCallbackQuery()
                .setText(text)
                .setCallbackQueryId(query.getId())
                .setShowAlert(showAsAlert)
                .call(handler);
    }

    private Map<Integer, Duel> getChatDuels(long chatId) {
        if (duels.containsKey(chatId)) {
            return duels.get(chatId);
        } else {
            var chatDuels = new HashMap<Integer, Duel>();
            duels.put(chatId, chatDuels);
            return chatDuels;
        }
    }

    static class Duel {
        private final long chatId;
        private final int messageId;

        private TgUser player1;
        private TgUser player2;

        Duel(long chatId, int messageId) {
            this.chatId = chatId;
            this.messageId = messageId;
        }
    }
}