package com.senderman.lastkatkabot.usercommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.telegram.telegrambots.meta.api.objects.Message
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class Weather (private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/weather"
    override val desc: String
        get() = "погода. Если не указать город, то покажет погоду в последнем введенном вами городе"

    override fun execute(message: Message) {
        val chatId = message.chatId
        var city: String? = message.text.trim().replace("/weather(:?@${handler.botUsername})?*\\s*".toRegex(), "")
        if (city!!.isBlank()) { // city is not specified
            city = Services.db.getUserCity(message.from.id)
            if (city == null) {
                handler.sendMessage(chatId, "Вы не указали город!")
                return
            }
        } else { // find a city
            try {
                val searchPage = Jsoup.parse(
                    URL(
                        "https://yandex.ru/pogoda/search?request=" + URLEncoder.encode(
                            city,
                            StandardCharsets.UTF_8
                        )
                    ), 10000
                )
                val table = searchPage.selectFirst("div.grid")
                val searchResult = table.selectFirst("li.place-list__item")
                city = searchResult.selectFirst("a").attr("href")
            } catch (e: NullPointerException) {
                handler.sendMessage(chatId, "Город не найден")
                return
            } catch (e: IOException) {
                handler.sendMessage(chatId, "Ошибка запроса")
            }
        }
        Services.db.setUserCity(message.from.id, city!!)
        val weatherPage: Document
        weatherPage = try {
            Jsoup.parse(URL("https://yandex.ru$city"), 10000)
        } catch (e: IOException) {
            handler.sendMessage(chatId, "Ошибка запроса")
            return
        }
        // parse weather
        val table = weatherPage.selectFirst("div.card_size_big")
        val title = weatherPage.selectFirst("h1.header-title__title").text()
        val temperature = table.selectFirst("div.fact__temp span.temp__value").text()
        val feelsLike = table.selectFirst("div.fact__feels-like div.term__value").text()
        val feelings = table.selectFirst("div.fact__feelings div.link__condition").text()
        val wind = table.selectFirst("div.fact__wind-speed div.term__value").text()
        val humidity = table.selectFirst("div.fact__humidity div.term__value").text()
        val pressure = table.selectFirst("div.fact__pressure div.term__value").text()
        val forecast = """
            <b>$title</b>
            
            $feelings
            🌡: $temperature °C
            🤔 Ощущается как $feelsLike
            💨: $wind
            💧: $humidity
            🧭: $pressure
            """.trimIndent()

        handler.sendMessage(chatId, forecast)
    }
}