package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class CakeOk(handler: LastkatkaBotHandler) : CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.CAKE_OK

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

        answerQuery(query, "П p u я т н o г o  a п п e т u т a", false)
        // message.text = username, username2 подарил вам тортик с чем-то
        val cakeInside = query.message.text.split("тортик", limit = 2)[1].trimStart()
        editText(
            query,
            "\uD83C\uDF82 ${query.from.firstName} принял тортик $cakeInside".trimEnd()
        )
    }
}