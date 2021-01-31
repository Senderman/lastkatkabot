package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class CakeNot(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.CAKE_NOT

    override fun handle(query: CallbackQuery) {
        if (query.from.id != query.message.replyToMessage.from.id) {
            answerQuery(query, "Этот тортик не вам!")
            return
        }
        if (query.message.date + 2400 < System.currentTimeMillis() / 1000) {
            answerQuery(query, "Тортик испортился!")
            editText(query, "\uD83E\uDD22 Тортик попытались взять, но он испортился!")
            return
        }

        answerQuery(query, "Ну и ладно", false)
        // message.text = username, username2 подарил вам тортик с чем-то
        val cakeInside = query.message.text.split("тортик", limit = 2)[1].trimStart()
        editText(
            query,
            "\uD83D\uDEAB \uD83C\uDF82 ${query.from.firstName} отказался от тортика $cakeInside".trimEnd()
        )
    }
}