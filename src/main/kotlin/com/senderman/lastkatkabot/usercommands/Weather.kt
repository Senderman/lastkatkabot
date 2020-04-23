package com.senderman.lastkatkabot.usercommands

import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.telegram.telegrambots.meta.api.objects.Message
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

class Weather(private val handler: LastkatkaBotHandler, private val db: DBService) : CommandExecutor {
    override val command: String
        get() = "/weather"
    override val desc: String
        get() = "погода. Если не указать город, то покажет погоду в последнем введенном вами городе"

    override fun execute(message: Message) {
        val chatId = message.chatId
        val city: String = message.text.trim().replace("/weather(:?@${handler.botUsername})?\\s*".toRegex(), "")

        val cityLink: String = if (city.isBlank()) {
            val cityLinkFromDb = db.getUserCity(message.from.id)
            if (cityLinkFromDb == null) {
                handler.sendMessage(chatId, "Вы не указали город! ( /weather город ). Бот запомнит ваш выбор.")
                return
            }
            cityLinkFromDb
        } else try { // find city
            getCityPageLink(city)
        } catch (e: NullPointerException) {
            handler.sendMessage(chatId, "Город не найден")
            return
        } catch (e: IOException) {
            handler.sendMessage(chatId, "Ошибка запроса")
            return
        }

        db.setUserCity(message.from.id, cityLink)
        val weatherPage: Document
        weatherPage = try { // open city's page from search results
            Jsoup.parse(URL("https://yandex.ru$cityLink"), 10000)
        } catch (e: IOException) {
            handler.sendMessage(chatId, "Ошибка запроса")
            return
        }
        val text: String = try {
            parseForecast(weatherPage).toString()
        } catch (e: Exception) {
            "Ошибка. Попробуйте уточнить запрос"
        }

        handler.sendMessage(chatId, text)
    }

    private fun getCityPageLink(city: String): String {
        val searchPage = Jsoup.parse(
            URL(
                "https://yandex.ru/pogoda/search?request=" + URLEncoder.encode(
                    city,
                    "UTF-8"
                )
            ), 10000
        )
        val table = searchPage.selectFirst("div.grid")
        val searchResult = table.selectFirst("li.place-list__item")
        return searchResult.selectFirst("a").attr("href")
    }

    private fun parseForecast(weatherPage: Element): Forecast {
        val title = weatherPage.selectFirst("h1.header-title__title").text()
        val table = weatherPage.selectFirst("div.card_size_big")
        val temperature = table.selectFirst("div.fact__temp span.temp__value").text()
        val feelsLike = table.selectFirst("div.fact__feels-like div.term__value").text()
        val feelings = table.selectFirst("div.fact__feelings div.link__condition").text()
        val wind = table.selectFirst("div.fact__wind-speed div.term__value").text()
        val humidity = table.selectFirst("div.fact__humidity div.term__value").text()
        val pressure = table.selectFirst("div.fact__pressure div.term__value").text()
        return Forecast(title, temperature, feelsLike, feelings, wind, humidity, pressure)
    }

    private data class Forecast(
        val title: String,
        val temperature: String,
        val feelsLike: String,
        val feelings: String,
        val wind: String,
        val humidity: String,
        val pressure: String
    ) {
        override fun toString() = """
            <b>$title</b>
            
            $feelings
            🌡: $temperature °C
            🤔 Ощущается как $feelsLike
            💨: $wind
            💧: $humidity
            🧭: $pressure
            """.trimIndent()
    }
}