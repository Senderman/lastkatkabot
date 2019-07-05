package com.senderman.lastkatkabot.Handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.senderman.lastkatkabot.LastResourceBundleLocalizationService;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import com.senderman.lastkatkabot.TempObjects.TgUser;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UsercommandsHandler {

    private final LastkatkaBotHandler handler;

    public UsercommandsHandler(LastkatkaBotHandler handler) {
        this.handler = handler;
    }

    static InlineKeyboardMarkup getMarkupForPayingRespects() {
        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                .setText("F")
                .setCallbackData(LastkatkaBot.CALLBACK_PAY_RESPECTS));
        markup.setKeyboard(List.of(row1));
        return markup;
    }

    public void action(Message message) {
        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
        if (message.getText().split("\\s+").length == 1)
            return;

        var action = message.getText().split("\\s+", 2)[1];
        var sm = Methods.sendMessage(message.getChatId(), message.getFrom().getFirstName() + " " + action);
        if (message.isReply())
            sm.setReplyToMessageId(message.getReplyToMessage().getMessageId());

        handler.sendMessage(sm);
    }

    public void payRespects(Message message) { // /f
        if (message.isUserMessage())
            return;

        if (message.getFrom().getFirstName().equals(message.getReplyToMessage().getFrom().getFirstName()))
            return;

        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
        var text = "\uD83D\uDD6F Press F to pay respects to " + message.getReplyToMessage().getFrom().getFirstName() +
                "\n" + message.getFrom().getFirstName() + " has payed respects";
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(text)
                .setReplyMarkup(getMarkupForPayingRespects()));
    }

    public void cake(Message message) {
        if (message.isUserMessage())
            return;

        var chatLocale = Services.i18n().getLocale(message);
        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                        .setText(Services.i18n().getString("acceptCake", chatLocale))
                        .setCallbackData(LastkatkaBot.CALLBACK_CAKE_OK + message.getText().replace("/cake", "")),
                new InlineKeyboardButton()
                        .setText(Services.i18n().getString("denyCake", chatLocale))
                        .setCallbackData(LastkatkaBot.CALLBACK_CAKE_NOT + message.getText().replace("/cake", "")));
        markup.setKeyboard(List.of(row1));
        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(String.format(Services.i18n().getString("cakeGift", chatLocale),
                        message.getReplyToMessage().getFrom().getFirstName(), message.getFrom().getFirstName(),
                        message.getText().replace("/cake", "")))
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup));
    }

    public void dice(Message message) {
        int random;
        var args = message.getText().split("\\s+", 3);
        if (args.length == 3) {
            try {
                int min = Integer.parseInt(args[1]);
                int max = Integer.parseInt(args[2]);
                random = ThreadLocalRandom.current().nextInt(min, max + 1);
            } catch (NumberFormatException nfe) {
                random = ThreadLocalRandom.current().nextInt(1, 7);
            }
        } else if (args.length == 2) {
            var max = Integer.parseInt(args[1]);
            random = ThreadLocalRandom.current().nextInt(1, max + 1);
        } else
            random = ThreadLocalRandom.current().nextInt(1, 7);

        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(Services.i18n().getString("diceThrown", message) + " " + random)
                .setReplyToMessageId(message.getMessageId()));
    }

    public void dstats(Message message) {
        var player = message.getFrom().getFirstName();
        var locale = Services.i18n().getLocale(message);
        var stats = Services.db().getStats(message.getFrom().getId());
        var wins = stats.get("wins");
        var total = stats.get("total");
        var winrate = (total == 0) ? 0 : 100 * wins / total;
        var bncwins = stats.get("bnc");
        var text = String.format(Services.i18n().getString("stats", locale),
                player, wins, total, winrate, bncwins);
        handler.sendMessage(message.getChatId(), text);

    }

    public void pinlist(Message message) {
        if (!isFromWwBot(message))
            return;
        Methods.Administration.pinChatMessage(message.getChatId(), message.getReplyToMessage().getMessageId())
                .setNotificationEnabled(false).call(handler);
        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
    }

    public void getinfo(Message message) {
        if (!message.isReply())
            return;
        handler.sendMessage(message.getChatId(), message.getReplyToMessage().toString()
                .replaceAll("[ ,]*\\w+='?null'?", "")
                .replaceAll("(\\w*[iI]d=)(-?\\d+)", "$1<code>$2</code>")
                .replaceAll("([{,])", "$1\n")
                .replaceAll("(})", "\n$1"));
    }

    public void testRegex(Message message) {
        var chatId = message.getChatId();
        var params = message.getText().split("\n");
        if (params.length != 3) {
            handler.sendMessage(chatId, Services.i18n().getString("argsError", message));
            return;
        }
        var regex = params[1];
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            handler.sendMessage(chatId, "Invalid regex");
            return;
        }
        handler.sendMessage(Methods.sendMessage(chatId, (params[2].matches(regex) ? "✅" : "❌"))
                .setReplyToMessageId(message.getMessageId()));
    }

    public void feedback(Message message) {
        var locale = Services.i18n().getLocale(message);
        var bugreport = Services.i18n().getString("bugreport", locale) +
                " <a href=\"tg://user?id=" +
                message.getFrom().getId() +
                "\">" +
                message.getFrom().getFirstName() +
                "</a>\n\n" +
                message.getText().replace("/feedback ", "");
        handler.sendMessage((long) Services.botConfig().getMainAdmin(), bugreport);
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText("✅" + Services.i18n().getString("bugreportSent", locale))
                .setReplyToMessageId(message.getMessageId()));
    }

    public void bnchelp(Message message) {
        var sendPhoto = Methods.sendPhoto()
                .setChatId(message.getChatId())
                .setFile(Services.botConfig().getBncphoto());
        if (message.isReply())
            sendPhoto.setReplyToMessageId(message.getReplyToMessage().getMessageId());
        else
            sendPhoto.setReplyToMessageId(message.getMessageId());
        sendPhoto.call(handler);
    }

    public void help(Message message) {
        var locale = Services.db().getUserLocale(message.getFrom().getId());
        var sb = new StringBuilder(Services.i18n().getString("help", locale));
        if (handler.admins.contains(message.getFrom().getId()))// admins want to get extra help
            sb.append("\n").append(Services.i18n().getString("adminHelp", locale));

        if (message.getFrom().getId().equals(Services.botConfig().getMainAdmin()))
            sb.append("\n").append(Services.i18n().getString("mainAdminHelp", locale));

        if (message.isUserMessage()) {
            var sm = Methods.sendMessage()
                    .setChatId(message.getChatId())
                    .setText(sb.toString());
            handler.sendMessage(sm);
            return;
        }

        // attempt to send help to PM
        try {
            handler.execute(new SendMessage((long) message.getFrom().getId(), sb.toString())
                    .setParseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            handler.sendMessage(Methods.sendMessage(message.getChatId(), Services.i18n().getString("pmplz", locale))
                    .setReplyToMessageId(message.getMessageId()));
            return;
        }
        handler.sendMessage(Methods.sendMessage(message.getChatId(), Services.i18n().getString("helpSent", locale))
                .setReplyToMessageId(message.getMessageId()));
    }

    public void setLocale(Message message) {
        var chatId = message.getChatId();
        if (!message.isUserMessage()) {
            var user = Methods.getChatMember(chatId, message.getFrom().getId()).call(handler);
            if (!user.getStatus().equals("creator") && !user.getStatus().equals("administrator")) {
                Methods.deleteMessage(chatId, message.getFrom().getId());
                return;
            }
        }

        if (message.getText().split(" ").length == 1) {

            var locales = new HashMap<String, String>();
            locales.put("ru", "\uD83C\uDDF7\uD83C\uDDFA Русский (by Senderman)");
            locales.put("en", "\uD83C\uDDEC\uD83C\uDDE7 English (by Senderman)");
            locales.put("uk", "\uD83C\uDDFA\uD83C\uDDE6 Українська (by crazy-man)");
            locales.put("uz", "\uD83C\uDDFA\uD83C\uDDFF O'zbek tili (by Jalilov_Shamshod)");

            var markup = new InlineKeyboardMarkup();
            var rows = new ArrayList<List<InlineKeyboardButton>>();
            for (var key : locales.keySet()) {
                rows.add(List.of(new InlineKeyboardButton()
                        .setText(locales.get(key))
                        .setCallbackData(LastkatkaBot.CALLBACK_SET_LANG + " " + key)));
            }
            markup.setKeyboard(rows);
            handler.sendMessage(new SendMessageMethod()
                    .setChatId(chatId)
                    .setText("Choose your language:")
                    .setReplyMarkup(markup));
        }
    }

    public void pair(Message message) {
        if (message.isUserMessage())
            return;

        var chatId = message.getChatId();
        var locale = Services.db().getChatLocale(message.getChatId());

        // check for existing pair
        if (Services.db().pairExistsToday(chatId)) {
            var pair = Services.db().getPairOfTheDay(chatId);
            pair = Services.i18n().getString("pairOfDay", locale) + " " + pair;
            handler.sendMessage(chatId, pair);
            return;
        }

        // remove users without activity for 2 weeks and get list of actual users
        Services.db().removeOldUsers(chatId, message.getDate() - 1209600);
        var userIds = Services.db().getChatMemebersIds(chatId);

        // generate 2 different random users
        TgUser user1, user2;
        try {
            user1 = getUserForPair(chatId, userIds);
            userIds.remove((Integer) user1.getId());
            user2 = getUserForPair(chatId, userIds);
        } catch (Exception e) {
            handler.sendMessage(chatId, Services.i18n().getString("noUsers", locale));
            return;
        }

        // get a phrase by locale and set up pair
        var loveI18n = new LastResourceBundleLocalizationService("Love", Services.db());
        var loveEntries = Integer.parseInt(loveI18n.getString("love", locale));
        var loveEntry = "l" + (1 + ThreadLocalRandom.current().nextInt(0, loveEntries));
        var loveStrings = loveI18n.getString(loveEntry, locale).split("\n");

        try {
            for (var i = 0; i < loveStrings.length - 1; i++) {
                handler.sendMessage(chatId, loveStrings[i]);
                Thread.sleep(1500);
            }
        } catch (InterruptedException e) {
            BotLogger.error("PAIR", "Ошибка таймера");
        }
        var pair = user1.getName() + " ❤ " + user2.getName();
        Services.db().setPair(chatId, pair);
        handler.sendMessage(chatId, String.format(loveStrings[loveStrings.length - 1], user1.getLink(), user2.getLink()));
    }

    private TgUser getUserForPair(long chatId, List<Integer> userIds) throws Exception {
        if (userIds.size() < 3)
            throw new Exception("Not enough users");

        ChatMember member;
        do {
            var random = ThreadLocalRandom.current().nextInt(userIds.size());
            var userId = userIds.get(random);
            member = Methods.getChatMember(chatId, userId).call(handler);
            // delete not-found-user
            if (member == null) {
                Services.db().removeUserFromChatDB(userId, chatId);
                userIds.remove(userId);
                if (userIds.size() < 3) {
                    throw new Exception("Not enough users");
                }
            }
        } while (member == null);

        return new TgUser(member.getUser().getId(), member.getUser().getFirstName());
    }

    public void lastpairs(Message message) {
        if (message.isUserMessage())
            return;

        var chatId = message.getChatId();
        var locale = Services.db().getChatLocale(message.getChatId());
        var history = Services.db().getPairsHistory(chatId);
        if (history == null)
            handler.sendMessage(chatId, Services.i18n().getString("neverPaired", locale));
        else
            handler.sendMessage(chatId, "<b>" + Services.i18n().getString("lastPairs", locale) + "</b>\n\n" + history);
    }

    private boolean isFromWwBot(Message message) {
        return Services.botConfig().getWwBots().contains(message.getReplyToMessage().getFrom().getUserName()) &&
                message.getReplyToMessage().getText().startsWith("#players");
    }
}
