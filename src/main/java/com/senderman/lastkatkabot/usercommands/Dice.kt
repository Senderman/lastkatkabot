package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.ThreadLocalRandom

class Dice constructor(val handler: LastkatkaBotHandler) : CommandExecutor {
    override val command: String
        get() = "/dice"
    override val desc: String
        get() = "бросить кубик. Можно указать диапазон, напр. /dice -5 9"

    override fun execute(message: Message) {
        val random: Int
        val args = message.text.split("\\s+".toRegex(), 3)
        random = when (args.size) {
            3 -> {
                try {
                    val min = args[1].toInt()
                    val max = args[2].toInt()
                    ThreadLocalRandom.current().nextInt(min, max + 1)
                } catch (nfe: NumberFormatException) {
                    ThreadLocalRandom.current().nextInt(1, 7)
                }
            }
            2 -> {
                val max = args[1].toInt()
                ThreadLocalRandom.current().nextInt(1, max + 1)
            }
            else -> ThreadLocalRandom.current().nextInt(1, 7)
        }
        handler.sendMessage(
            Methods.sendMessage()
                .setChatId(message.chatId)
                .setText("\uD83C\uDFB2 Кубик брошен. Результат: $random")
                .setReplyToMessageId(message.messageId)
        )
    }
}