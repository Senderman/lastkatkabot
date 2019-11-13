package com.senderman.anitrackerbot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;
import java.util.Objects;

public class AnitrackerBotHandler extends BotHandler {

    private final BotConfig config;

    AnitrackerBotHandler(BotConfig config) {
        this.config = config;
        Services.setBotConfig(config);
        Services.setDataBase(new MongoDBService());
    }

    @Override
    protected BotApiMethod onUpdate(@NotNull Update update) {

        if (!update.hasMessage())
            return null;

        final var message = update.getMessage();

        // don't handle old messages
        if (message.getDate() + 120 < System.currentTimeMillis() / 1000)
            return null;

        if (!message.hasText())
            return null;

        final var chatId = message.getChatId();
        final var userId = message.getFrom().getId();

        final var text = message.getText();

        var command = text.split(" ", 2)[0].toLowerCase(Locale.ENGLISH).replace("@" + getBotUsername(), "");

        if (command.contains("@"))
            return null;

        switch (command) {
            case "/help":
                sendMessage(chatId, config.getHelp(), false);
                return null;
            case "/list":
                sendMessage(chatId, "Парсим ваше аниме, это может занять время...");
                sendMessage(chatId, listAnimes(userId), false);
                break;
            case "/del":
                try {
                    var id = Integer.parseInt(text.split(" ")[1]);
                    if (!Services.db().idExists(id, userId)) {
                        sendMessage(chatId, "Id не существует!");
                    } else {
                        Services.db().deleteAnime(id, userId);
                        sendMessage(chatId, "Аниме удалено из списка!");
                        if (Services.db().totalAnimes(userId) == 0) {
                            Services.db().dropUser(userId);
                        }
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            case "/dl":
                try {
                    var id = Integer.parseInt(text.split(" ")[1]);
                    if (!Services.db().idExists(id, userId)) {
                        sendMessage(chatId, "Id не существует!");
                        return null;
                    }
                    var url = Services.db().getAnimeUrl(id, userId);
                    var parser = getAnimeParser(url);
                    var anime = Objects.requireNonNull(parser).parse(url);
                    var downloader = getAnimeDownloader(url);
                    if (downloader == null) {
                        sendMessage(chatId, "Отсюда аниме скачивать я еще не умею!");
                        return null;
                    }
                    sendMessage(chatId, "Скачиваем торрент...");
                    var file = downloader.download(url);
                    Methods.sendDocument(chatId)
                            .setFile(file)
                            .setCaption(anime.getTitle())
                            .call(this);
                    file.delete();
                } catch (Exception e) {
                    sendMessage(chatId, "Ошибка!");
                    e.printStackTrace();
                }
                return null;
        }

        if (!text.startsWith("http"))
            return null;

        AnimeParser parser = getAnimeParser(text);
        if (parser == null)
            return null;

        Anime anime;
        try {
            anime = parser.parse(text);
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка парсинга\n" + e.toString());
            return null;
        }

        sendMessage(chatId, parseAnimeData(anime, true));

        if (Services.db().urlExists(text, userId)) {
            sendMessage(chatId, "Это аниме уже есть в вашем списке!");
            return null;
        }

        int animeId = Services.db().totalAnimes(userId);
        if (animeId == 20) {
            sendMessage(chatId, "У вас и так много аниме!");
            return null;
        }

        while (Services.db().idExists(animeId, userId))
            animeId++;

        Services.db().saveAnime(animeId, userId, text);
        sendMessage(chatId, "Аниме добавлено в список!");

        return null;
    }

    private void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, true);
    }

    private void sendMessage(long chatId, String text, boolean enablePreview) {
        var sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText(text)
                .setParseMode(ParseMode.HTML);
        if (!enablePreview)
            sm.disableWebPagePreview();
        sm.call(this);
    }

    private AnimeParser getAnimeParser(String url) {
        if (url.startsWith("https://tr.anidub.com"))
            return AnimeParsers::parseAnidub;
        else if (url.startsWith("https://smotretanime.ru"))
            return AnimeParsers::parseSMAnime;
        else if (url.startsWith("http://gidfilm.ru/anime/"))
            return AnimeParsers::parseGidfilm;
        else if (url.startsWith("https://animerost.org/"))
            return AnimeParsers::parseAnimerost;
        else if (url.startsWith("https://anistar.org/"))
            return AnimeParsers::parseAnistar;
        else if (url.startsWith("https://nyaa.si/view/"))
            return AnimeParsers::parseNyaasi;
        else
            return null;
    }

    private AnimeDownloader getAnimeDownloader(String url) {
        if (url.startsWith("https://tr.anidub.com"))
            return AnimeDownloaders::getAnidubTorrent;
        else if (url.startsWith("https://anistar.org/"))
            return AnimeDownloaders::getAnistarTorrent;
        else if (url.startsWith("https://nyaa.si/view/"))
            return AnimeDownloaders::getNyaasiTorrent;
        else
            return null;
    }

    private String listAnimes(int userId) {
        var text = new StringBuilder("<b>Ваше аниме:</b>\n\n");
        var animes = Services.db().getAllAnimes(userId);
        for (var animeId : animes.keySet()) {
            var url = animes.get(animeId);
            var parser = getAnimeParser(url);
            if (parser == null)
                break;
            try {
                text.append(parseAnimeData(parser.parse(url), false))
                        .append("<b>URL:</b> ").append(url).append("\n")
                        .append("<b>Id:</b> ").append(animeId).append("\n\n");
            } catch (Exception ignored) {
            }
        }
        return text.toString();
    }

    private String parseAnimeData(Anime anime, boolean addImg) {
        var text = "<b>Название:</b> " + anime.getTitle() + "\n" +
                "<b>Серии:</b> " + anime.getSeries() + "\n";
        if (addImg)
            text += "<a href=\"" + anime.getImg() + "\">\u200B</a>";
        return text;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername().split(" ")[config.getPosition()];
    }

    @Override
    public String getBotToken() {
        return config.getToken().split(" ")[config.getPosition()];
    }
}
