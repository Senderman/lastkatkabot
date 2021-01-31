package com.senderman.lastkatkabot.admincommands

import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

class RunAPI(private val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/api"
    override val desc: String
        get() = """
            запуск метода из Telegram API. Синтаксис:
            /api
            метод
            ключ=значение
            ключ=значение
            итд
        """.trimIndent()
    override val forMainAdmin: Boolean
        get() = true

    override fun execute(message: Message) {
        val chatId = message.chatId
        val params = message.text.trim().split("\n")
        if (params.size < 2) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }
        val method = params[1]
        val request = try {
            if (params.size == 2)
                buildRequest(method, emptyList())
            else
                buildRequest(method, params.subList(2, params.size))
        } catch (e: Exception) {
            handler.sendMessage(chatId, "Неверное значение: ${e.message}")
            return
        }

        var response = try {
            makeRequest(request)
        } catch (e: IOException) {
            "Ошибка запроса"
        }

        response = LastkatkaBot.formatJSON(response)
        handler.sendMessage(chatId, "Ответ сервера:\n\n$response")
    }

    private fun makeRequest(request: String): String {
        val connection = URL(request).openConnection()
        val input = connection.getInputStream()
        val baos = ByteArrayOutputStream()
        var length: Int
        val buffer = ByteArray(1024)
        while (input.read(buffer).also { length = it } != -1) {
            baos.write(buffer, 0, length)
        }
        input.close()
        return baos.toString()
    }

    /**
     * @throws Exception with message="key=value" if the kay-value pair in invalid
     * @return String which could be used as GET request
     */
    private fun buildRequest(method: String, keyValues: List<String>): String {
        val sb = StringBuilder("https://api.telegram.org/bot")
        sb.append(handler.botToken).append("/").append(method).append("?")
        for (keyValue in keyValues) {
            if (!keyValue.trim().matches("\\w+=.+".toRegex()))
                throw Exception(keyValue)
            val kv = keyValue.split("=".toRegex(), 2)
            val key = kv[0]
            val value = kv[1]
            sb.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"))
            sb.append("&")
        }
        // remove trailing "&" or "?"
        sb.deleteCharAt(sb.lastIndex)
        return sb.toString()
    }
}