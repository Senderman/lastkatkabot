package com.senderman.lastkatkabot.tempobjects;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.TgUser;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Duel {

    private final long chatId;
    private final int messageId;
    private final TgUser player1;
    private TgUser player2;

    public Duel(Message message) {
        chatId = message.getChatId();
        player1 = new TgUser(message.getFrom().getId(), message.getFrom().getFirstName());
        var sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText("\uD83C\uDFAF Набор на дуэль! Жмите кнопку ниже\nДжойнулись:\n" + player1.getName());
        messageId = sm.call(Services.handler()).getMessageId();
        setReplyMarkup(chatId, messageId);
    }

    public void join(CallbackQuery query) {
        if (player2 != null) {
            answerCallbackQuery(query, "\uD83D\uDEAB Дуэлянтов уже набрали, увы", true);
            return;
        }
        if (query.getFrom().getId().equals(player1.getId())) {
            answerCallbackQuery(query, "\uD83D\uDC7A Я думаю, что тебе стоит сходить к психологу! Ты вызываешь на дуэль самого себя", true);
            return;
        }

        player2 = new TgUser(query.getFrom().getId(), query.getFrom().getFirstName());
        answerCallbackQuery(query, "✅ Вы успешно присоединились к дуэли!", false);
        start();
    }

    private void start() {
        var randomInt = ThreadLocalRandom.current().nextInt(100);
        var winner = (randomInt < 50) ? player1 : player2;
        var loser = (randomInt < 50) ? player2 : player1;

        var winnerName = winner.getName();
        var loserName = loser.getName();

        var duelResult = new StringBuilder();
        duelResult.append(String.format("<b>Дуэль</b>\n" +
                        "%1$s vs %2$s\n" +
                        "Противники разошлись в разные стороны, развернулись лицом друг к другу, и %3$s выстрелил первым\n" +
                        "%4$s лежит на земле, истекая кровью!\n\n",
                player1.getName(), player2.getName(), winnerName, loserName));

        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            duelResult.append(String.format("Но, умирая, %1$s успевает выстрелить в голову %2$s!\n" +
                            "%2$s падает замертво!\n" +
                            "\uD83D\uDC80 <b>Дуэль окончилась ничьей!</b>",
                    loserName, winnerName));
            Services.db().incTotalDuels(winner.getId());
            Services.db().incTotalDuels(loser.getId());

        } else {
            duelResult.append(String.format("\uD83D\uDC51 <b>%1$s выиграл дуэль!</b>", winnerName));
            Services.db().incDuelWins(winner.getId());
            Services.db().incTotalDuels(loser.getId());
        }

        Methods.editMessageText()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setText(duelResult.toString())
                .setParseMode(ParseMode.HTML)
                .call(Services.handler());
        Services.handler().duels.get(chatId).remove(messageId);
    }

    public int getMessageId() {
        return messageId;
    }

    public static void answerCallbackQuery(CallbackQuery query, String text, boolean showAsAlert) {
        Methods.answerCallbackQuery()
                .setText(text)
                .setCallbackQueryId(query.getId())
                .setShowAlert(showAsAlert)
                .call(Services.handler());
    }

    private static void setReplyMarkup(long chatId, int duelMessageId) {
        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                .setText("Присоединиться")
                .setCallbackData(LastkatkaBot.CALLBACK_JOIN_DUEL));
        markup.setKeyboard(List.of(row1));
        Methods.editMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(duelMessageId)
                .setReplyMarkup(markup)
                .call(Services.handler());
    }
}
