package com.senderman.lastkatkabot.Handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import com.senderman.lastkatkabot.TempObjects.BnCPlayer;
import com.senderman.lastkatkabot.TempObjects.TgUser;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

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

        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                        .setText("Принять")
                        .setCallbackData(LastkatkaBot.CALLBACK_CAKE_OK + message.getText().replace("/cake", "")),
                new InlineKeyboardButton()
                        .setText("Отказаться")
                        .setCallbackData(LastkatkaBot.CALLBACK_CAKE_NOT + message.getText().replace("/cake", "")));
        markup.setKeyboard(List.of(row1));
        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(String.format("\uD83C\uDF82 %1$s, пользователь %2$s подарил вам тортик %3$s",
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
                .setText("\uD83C\uDFB2 Кубик брошен. Результат: " + random)
                .setReplyToMessageId(message.getMessageId()));
    }

    public void dstats(Message message) {
        var player = message.getFrom().getFirstName();
        var stats = Services.db().getStats(message.getFrom().getId());
        var wins = stats.get("wins");
        var total = stats.get("total");
        var winrate = (total == 0) ? 0 : 100 * wins / total;
        var bncwins = stats.get("bnc");
        var text = String.format("\uD83D\uDCCA Статистика %1$s:\n\n" +
                        "Дуэлей выиграно: %2$d\n" +
                        "Всего дуэлей: %3$d\n" +
                        "Винрейт: %4$d\n" +
                        "\n" +
                        "\uD83D\uDC2E Баллов за быки и коровы: %5$d",
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
            handler.sendMessage(chatId, "Неверное количество аргументов!");
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
        var user = new TgUser(message.getFrom().getId(), message.getFrom().getFirstName());
        var bugreport = "⚠️ <b>Фидбек</b>\n\n" +
                "От:" +
                user.getLink() + "\n\n"
                +
                message.getText().replace("/feedback ", "");
        handler.sendMessage((long) Services.botConfig().getMainAdmin(), bugreport);
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText("✅ Отправлено разрабу бота!")
                .setReplyToMessageId(message.getMessageId()));
    }

    public void bncTop(Message message) {
        var chatId = message.getChatId();

        handler.sendMessage(chatId, "Сортируем список, находим имена...");
        List<BnCPlayer> top = Services.db().getTop();
        var text = new StringBuilder("<b>Топ-10 задротов в bnc:</b>\n\n");
        int counter = 1;
        for (var player : top) {
            try {
                var userChatId = Services.db().findChatWithUser(player.getId());
                var member = Methods.getChatMember(userChatId, player.getId()).call(handler);
                player.setName(member.getUser().getFirstName());

            } catch (Exception ignored) {
            }
            text.append(counter).append(": ");
            if (message.isUserMessage())
                text.append(player.getLink());
            else
                text.append(player.getName());
            text.append(" (").append(player.getScore()).append(")\n");
            counter++;
        }
        handler.sendMessage(chatId, text.toString());
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
        var sb = new StringBuilder(Services.botConfig().getHelp());
        if (handler.admins.contains(message.getFrom().getId()))// admins want to get extra help
            sb.append("\n").append(Services.botConfig().getAdminHelp());

        if (message.getFrom().getId().equals(Services.botConfig().getMainAdmin()))
            sb.append("\n").append(Services.botConfig().getMainAdminHelp());

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
            handler.sendMessage(Methods.sendMessage(message.getChatId(), "Пожалуйста, начните диалог со мной в лс")
                    .setReplyToMessageId(message.getMessageId()));
            return;
        }
        handler.sendMessage(Methods.sendMessage(message.getChatId(), "✅ Помощь была отправлена вам в лс")
                .setReplyToMessageId(message.getMessageId()));
    }


    public void pair(Message message) {
        if (message.isUserMessage())
            return;

        var chatId = message.getChatId();

        // check for existing pair
        if (Services.db().pairExistsToday(chatId)) {
            var pair = Services.db().getPairOfTheDay(chatId);
            pair = "Пара дня: " + pair;
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
            handler.sendMessage(chatId, "Недостаточно пользователей для создания пары! Подождите, пока кто-то еще напишет в чат!");
            return;
        }

        // get a random text and set up a pair
        var loveArray = Services.botConfig().getLoveStrings();
        var loveStrings = loveArray[ThreadLocalRandom.current().nextInt(loveArray.length)].split("\n");

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
        var history = Services.db().getPairsHistory(chatId);
        if (history == null)
            handler.sendMessage(chatId, "В этом чате еще никогда не запускали команду /pair!");
        else
            handler.sendMessage(chatId, "<b>Последние 10 пар:</b>\n\n" + history);
    }

    private boolean isFromWwBot(Message message) {
        return Services.botConfig().getWwBots().contains(message.getReplyToMessage().getFrom().getUserName()) &&
                message.getReplyToMessage().getText().startsWith("#players");
    }
}
