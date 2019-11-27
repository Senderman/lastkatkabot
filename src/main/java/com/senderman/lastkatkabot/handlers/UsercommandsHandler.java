package com.senderman.lastkatkabot.handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.Command;
import com.senderman.TgUser;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import com.senderman.lastkatkabot.tempobjects.BnCPlayer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    public void pressF(Message message) { // /f
        if (message.isUserMessage())
            return;

        if (message.isReply() && message.getFrom().getFirstName().equals(message.getReplyToMessage().getFrom().getFirstName()))
            return;

        var object = message.getText().split(" ").length > 1
                ? message.getText().split(" ", 2)[1]
                : message.getReplyToMessage().getFrom().getFirstName();

        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);
        var text = "\uD83D\uDD6F Press F to pay respects to " + object +
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
        var presenter = new TgUser(message.getFrom());
        var luckyOne = new TgUser(message.getReplyToMessage().getFrom());
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText(String.format("\uD83C\uDF82 %1$s, пользователь %2$s подарил вам тортик %3$s",
                        luckyOne.getName(), presenter.getName(),
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

    public void marryme(Message message) {
        if (!message.isReply() || message.getFrom().getId().equals(message.getReplyToMessage().getFrom().getId()) || message.getReplyToMessage().getFrom().getBot())
            return;

        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var loverId = Services.db().getLover(userId);
        if (loverId != 0) {
            handler.sendMessage(chatId, "Всмысле? Вы что, хотите изменить своей второй половинке?!");
            return;
        }

        if (Services.db().getLover(message.getReplyToMessage().getFrom().getId()) != 0) {
            handler.sendMessage(chatId, "У этого пользователя уже есть своя вторая половинка!");
            return;
        }

        var markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(
                new InlineKeyboardButton()
                        .setText("Принять")
                        .setCallbackData(LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE + userId),
                new InlineKeyboardButton()
                        .setText("Отказаться")
                        .setCallbackData(LastkatkaBot.CALLBACK_DENY_MARRIAGE)
        )));
        var user = new TgUser(Methods.getChatMember(chatId, userId).call(handler).getUser());
        var text = "Пользователь " + user.getLink() + " предлагает вам руку, сердце и шавуху. Вы согласны?";
        var sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup);
        handler.sendMessage(sm);
    }

    public void divorce(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();
        var loverId = Services.db().getLover(userId);
        if (loverId == 0) {
            handler.sendMessage(chatId, "У вас и так никого нет!");
            return;
        }

        Services.db().divorce(userId);
        handler.sendMessage(chatId, "Вы расстались со своей половинкой! А ведь так все хорошо начиналось...");
        handler.sendMessage(loverId, "Ваша половинка покинула вас... Теперь вы одни,,,");
    }

    public void stats(Message message) {
        var player = !message.isReply() ? message.getFrom() : message.getReplyToMessage().getFrom();
        if (player.getBot()) {
            handler.sendMessage(message.getChatId(), "Но это же просто бот, имитация человека!" +
                    "Разве может бот написать симфонию, иметь статистику, играть в BnC, любить?");
            return;
        }
        var user = new TgUser(player);
        var stats = Services.db().getStats(player.getId());
        var wins = stats.get("wins");
        var total = stats.get("total");
        var winrate = (total == 0) ? 0 : 100 * wins / total;
        var bncwins = stats.get("bnc");
        var lover = stats.get("lover");
        var text = String.format("\uD83D\uDCCA Статистика %1$s:\n\n" +
                        "Дуэлей выиграно: %2$d\n" +
                        "Всего дуэлей: %3$d\n" +
                        "Винрейт: %4$d\n" +
                        "\n" +
                        "\uD83D\uDC2E Баллов за быки и коровы: %5$d",
                user.getName(), wins, total, winrate, bncwins);
        if (lover != 0) {
            text += "\n❤️ Вторая половинка: " +
                    new TgUser(Methods.getChatMember(lover, lover).call(handler).getUser()).getLink();
        }
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

    public void weather(Message message) {
        var chatId = message.getChatId();
        String city = message.getText().strip().replaceAll("^/weather\\s*", "");
        if (city.isBlank()) { // city is not specified
            city = Services.db().getUserCity(message.getFrom().getId());
            if (city == null) {
                handler.sendMessage(chatId, "Вы не указали город!");
                return;
            }
        } else {
            // find a city
            try {
                var searchPage = Jsoup.parse(new URL("https://yandex.ru/pogoda/search?request=" + URLEncoder.encode(city, StandardCharsets.UTF_8)), 10000);
                var table = searchPage.selectFirst("div.grid");
                var searchResult = table.selectFirst("li.place-list__item");
                city = searchResult.selectFirst("a").attr("href");
            } catch (NullPointerException e) {
                handler.sendMessage(chatId, "Город не найден");
                return;
            } catch (IOException e) {
                handler.sendMessage(chatId, "Ошибка запроса");
            }
        }

        Services.db().setUserCity(message.getFrom().getId(), city);
        Document weatherPage;
        try {
            weatherPage = Jsoup.parse(new URL("https://yandex.ru" + city), 10000);
        } catch (IOException e) {
            handler.sendMessage(chatId, "Ошибка запроса");
            return;
        }

        // parse weather
        var table = weatherPage.selectFirst("div.card_size_big");
        var title = weatherPage.selectFirst("h1.header-title__title").text();
        var temperature = "\uD83C\uDF21: " + table.selectFirst("div.fact__temp").selectFirst("span.temp__value").text() + " °C";
        var feelsLike = "\uD83E\uDD14 Ощущается как " + table.selectFirst("div.fact__feels-like").selectFirst("div.term__value").text();
        var feelings = table.selectFirst("div.fact__feelings").selectFirst("div.link__condition").text();
        var wind = "\uD83D\uDCA8: " + table.selectFirst("div.fact__wind-speed").selectFirst("div.term__value").text();
        var humidity = "\uD83D\uDCA7: " + table.selectFirst("div.fact__humidity").selectFirst("div.term__value").text();
        var pressure = "\uD83E\uDDED: " + table.selectFirst("div.fact__pressure").selectFirst("div.term__value").text();

        String forecast = "<b>" + title + "</b>\n\n" +
                String.join("\n", feelings, temperature, feelsLike, wind, humidity, pressure);
        handler.sendMessage(chatId, forecast);
    }

    public void feedback(Message message) {
        var user = new TgUser(message.getFrom());
        var bugreport = "⚠️ <b>Фидбек</b>\n\n" +
                "От: " +
                user.getLink() + "\n\n"
                +
                message.getText().replace("/feedback ", "");
        handler.sendMessage(Services.config().getMainAdmin(), bugreport);
        handler.sendMessage(Methods.sendMessage()
                .setChatId(message.getChatId())
                .setText("✅ Отправлено разрабу бота!")
                .setReplyToMessageId(message.getMessageId()));
    }

    public void bncTop(Message message) {
        var chatId = message.getChatId();

        List<BnCPlayer> top = Services.db().getTop();
        var text = new StringBuilder("<b>Топ-10 задротов в bnc:</b>\n\n");
        int counter = 1;
        for (var player : top) {
            var member = Methods.getChatMember(player.getId(), player.getId()).call(handler);
            player.setName(member.getUser().getFirstName());
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

    public void bncHelp(Message message) {
        var sendPhoto = Methods.sendPhoto()
                .setChatId(message.getChatId())
                .setFile(Services.config().getBncphoto());
        if (message.isReply())
            sendPhoto.setReplyToMessageId(message.getReplyToMessage().getMessageId());
        else
            sendPhoto.setReplyToMessageId(message.getMessageId());
        sendPhoto.call(handler);
    }

    public void help(Message message) {
        var help = new StringBuilder("Привет! Это очень полезный бот для проекта @lastkatka, который многое что умеет! Основные команды:\n\n");
        var adminHelp = new StringBuilder("<b>Информация для админов бота</b>\n\n");
        var mainAdminHelp = new StringBuilder("<b>Информация для главного админа бота</b>\n\n");
        var noobId = message.getFrom().getId();

        for (var m : handler.commands.values()) {
            var annotation = m.getAnnotation(Command.class);
            if (!annotation.showInHelp())
                continue;

            var helpLine = annotation.name() + " - " + annotation.desc() + "\n";
            if (noobId.equals(Services.config().getMainAdmin()) && annotation.forMainAdmin())
                mainAdminHelp.append(helpLine);
            else if (handler.isFromAdmin(message) && annotation.forAllAdmins())
                adminHelp.append(helpLine);
            else
                help.append(helpLine);
            // TODO add help for premium users when needed
        }

        if (handler.isFromAdmin(message))
            help.append("\n").append(adminHelp);
        if (noobId.equals(Services.config().getMainAdmin()))
            help.append("\n").append(mainAdminHelp);

        // attempt to send help to PM
        try {
            handler.execute(new SendMessage((long) message.getFrom().getId(), help.toString())
                    .setParseMode(ParseMode.HTML));
        } catch (TelegramApiException e) {
            handler.sendMessage(Methods.sendMessage(message.getChatId(), "Пожалуйста, начните диалог со мной в лс")
                    .setReplyToMessageId(message.getMessageId()));
            return;
        }
        if (!message.isUserMessage())
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
            user2 = getUserForPair(chatId, userIds, user1);
        } catch (Exception e) {
            handler.sendMessage(chatId, "Недостаточно пользователей для создания пары! Подождите, пока кто-то еще напишет в чат!");
            return;
        }

        // get a random text and set up a pair
        var loveArray = Services.config().getLoveStrings();
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

    private TgUser getUserForPair(long chatId, List<Integer> userIds, TgUser first) throws Exception {
        var loverId = Services.db().getLover(first.getId());
        if (userIds.contains(loverId)) {
            return new TgUser(Methods.getChatMember(chatId, loverId).call(handler).getUser());
        }
        return getUserForPair(chatId, userIds);
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
        } while (member == null || member.getUser().getFirstName().isBlank());

        return new TgUser(member.getUser());
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
        return Services.config().getWwBots().contains(message.getReplyToMessage().getFrom().getUserName()) &&
                message.getReplyToMessage().getText().startsWith("#players");
    }
}
