package com.senderman.anitrackerbot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.TgUser
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

class AnitrackerBotHandler internal constructor(private val config: BotConfig) : BotHandler() {

    init {
        Services.botConfig = config
        Services.db = MongoDBService()
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {
        if (!update.hasMessage()) return null
        val message = update.message
        // don't handle old messages
        if (message.date + 120 < System.currentTimeMillis() / 1000) return null
        if (!message.hasText()) return null

        val chatId = message.chatId
        val userId = message.from.id
        val text = message.text

        val command = text.split(" ".toRegex(), 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@$botUsername", "")
        if ("@" in command) return null


        when (command) {
            "/help" -> sendMessage(chatId, config.help, false)

            "/list" -> {
                sendMessage(chatId, "Парсим ваше аниме, это может занять время...")
                sendMessage(chatId, listAnimes(userId), false)
            }

            "/del" -> {
                return try {
                    val id = text.split(" ")[1].toInt()
                    if (!Services.db.idExists(id, userId)) {
                        sendMessage(chatId, "Id не существует!")
                    } else {
                        Services.db.deleteAnime(id, userId)
                        sendMessage(chatId, "Аниме удалено из списка!")
                        if (Services.db.totalAnimes(userId) == 0) {
                            Services.db.dropUser(userId)
                        }
                    }
                    null
                } catch (e: Exception) {
                    null
                }
            }

            "/dl" -> {
                try {
                    val id = text.split(" ")[1].toInt()
                    if (!Services.db.idExists(id, userId)) {
                        sendMessage(chatId, "Id не существует!")
                        return null
                    }

                    val url = Services.db.getAnimeUrl(id, userId)
                    val parser = getAnimeParser(url)!!
                    val (title) = parser.parse(url)
                    val downloader = getAnimeDownloader(url)
                    if (downloader == null) {
                        sendMessage(chatId, "Отсюда аниме скачивать я еще не умею!")
                        return null
                    }
                    sendMessage(chatId, "Скачиваем торрент...")
                    val file = downloader.download(url)
                    Methods.sendDocument(chatId)
                            .setFile(file)
                            .setCaption(title)
                            .call(this)
                    file.delete()
                } catch (e: Exception) {
                    sendMessage(chatId, "Ошибка!")
                }
                return null
            }

            "/get" -> {
                val textArr = text.split(" ".toRegex(), 2)
                if (textArr.size < 2) return null

                val url = textArr[1]
                val downloader = getAnimeDownloader(url)
                if (downloader == null) {
                    sendMessage(chatId, "Отсюда аниме скачивать я еще не умею!")
                    return null
                }

                sendMessage(chatId, "Скачиваем торрент...")
                try {
                    val file = downloader.download(url)
                    Methods.sendDocument(chatId)
                            .setFile(file)
                            .call(this)
                    file.delete()
                } catch (e: Exception) {
                    sendMessage(chatId, "Ошибка!")
                }
                return null
            }

            "/users" -> {
                val userIds = Services.db.getUsersIds()
                val textList = StringBuilder("Список пользователей:\n\n")
                for (id in userIds) {
                    val user = Methods.getChatMember(id.toLong(), id).call(this).user
                    val tgUser = TgUser(id, user.firstName)
                    textList.append("${tgUser.link}\n")
                }
                sendMessage(chatId, textList.toString())
            }
        }

        if (!text.startsWith("http")) return null
        val parser = getAnimeParser(text) ?: return null

        val anime = try {
            parser.parse(text)
        } catch (e: Exception) {
            sendMessage(chatId, "Ошибка парсинга\n$e")
            return null
        }

        sendMessage(chatId, parseAnimeData(anime, true))

        if (Services.db.urlExists(text, userId)) {
            sendMessage(chatId, "Это аниме уже есть в вашем списке!")
            return null
        }

        var animeId = Services.db.totalAnimes(userId)
        if (animeId == 20) {
            sendMessage(chatId, "У вас и так много аниме!")
            return null
        }

        while (Services.db.idExists(animeId, userId)) animeId++
        Services.db.saveAnime(animeId, userId, text)
        sendMessage(chatId, "Аниме добавлено в список!")
        return null
    }

    private fun sendMessage(chatId: Long, text: String?, enablePreview: Boolean = true) {
        val sm = Methods.sendMessage()
                .setChatId(chatId)
                .setText(text!!)
                .setParseMode(ParseMode.HTML)
        if (!enablePreview) sm.disableWebPagePreview()
        sm.call(this)
    }

    private fun getAnimeParser(url: String): AnimeParser? {
        val decapUrl = url.decapitalize()
        fun String.sw(s: String): Boolean = this.startsWith(s)
        return when {
            decapUrl.sw("https://tr.anidub.com") -> AnimeParser { u -> AnimeParsers.parseAnidub(u) }
            decapUrl.sw("https://smotretanime.ru") -> AnimeParser { u -> AnimeParsers.parseSMAnime(u) }
            decapUrl.sw("http://gidfilm.ru/anime/") -> AnimeParser { u -> AnimeParsers.parseGidfilm(u) }
            decapUrl.sw("https://animerost.org/") -> AnimeParser { u -> AnimeParsers.parseAnimerost(u) }
            decapUrl.sw("https://anistar.org/") -> AnimeParser { u -> AnimeParsers.parseAnistar(u) }
            decapUrl.sw("https://nyaa.si/view/") -> AnimeParser { u -> AnimeParsers.parseNyaasi(u) }
            else -> null
        }
    }

    private fun getAnimeDownloader(url: String): AnimeDownloader? {
        val decapUrl = url.decapitalize()
        fun String.sw(s: String): Boolean = this.startsWith(s)
        return when {
            decapUrl.sw("https://tr.anidub.com") -> AnimeDownloader { u -> AnimeDownloaders.getAnidubTorrent(u) }
            decapUrl.sw("https://anistar.org/") -> AnimeDownloader { u -> AnimeDownloaders.getAnistarTorrent(u) }
            decapUrl.sw("https://nyaa.si/view/") -> AnimeDownloader { u -> AnimeDownloaders.getNyaasiTorrent(u) }
            else -> null
        }
    }

    private fun listAnimes(userId: Int): String {
        val text = StringBuilder("<b>Ваше аниме:</b>\n\n")
        val animes = Services.db.getAllAnimes(userId)
        for ((animeId, url) in animes) {
            val parser = getAnimeParser(url) ?: break
            try {
                text.append(parseAnimeData(parser.parse(url), false))
                        .append("<b>URL:</b> ").append(url).append("\n")
                        .append("<b>Id:</b> ").append(animeId).append("\n\n")
            } catch (ignored: Exception) {
            }
        }
        return text.toString()
    }

    private fun parseAnimeData(anime: Anime, addImg: Boolean): String {
        var text = "<b>Название:</b> ${anime.title}\n" +
                "<b>Серии:</b> ${anime.series}\n"
        if (addImg) text += "<a href=\"${anime.img}\">\u200B</a>"
        return text
    }

    override fun getBotUsername(): String {
        return config.username.split(" ")[config.position]
    }

    override fun getBotToken(): String {
        return config.token.split(" ")[config.position]
    }
}