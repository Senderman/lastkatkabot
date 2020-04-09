package com.senderman.lastkatkabot.handlers/*package com.senderman.lastkatkabot.handlers


import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class CallbackHandler(private val handler: LastkatkaBotHandler) {


    // TODO implement
    /*fun registerInTournament(query: CallbackQuery) {
        val memberId = query.from.id
        if (!handler.tournamentHandler.isEnabled) {
            answerQuery(query, "⚠️ На данный момент нет открытых раундов!")
            return
        }
        if (memberId in handler.tournamentHandler.membersIds) {
            answerQuery(query, "⚠️ Вы уже получили разрешение на отправку сообщений!")
            return
        }
        if (query.from.userName !in handler.tournamentHandler.members) {
            answerQuery(query, "\uD83D\uDEAB Вы не являетесь участником текущего раунда!")
            return
        }
        handler.tournamentHandler.membersIds.add(memberId)
        Methods.Administration.restrictChatMember()
                .setChatId(Services.botConfig.tourgroup)
                .setUserId(memberId)
                .setCanSendMessages(true)
                .setCanSendMediaMessages(true)
                .setCanSendOtherMessages(true)
                .call(handler)
        answerQuery(query, "✅ Вам даны права на отправку сообщений в группе турнира!")
        handler.sendMessage(Services.botConfig.tourgroup, "✅ ${query.from.firstName} получил доступ к игре!")
    }*/



    fun acceptChild(query: CallbackQuery) {
        val userId = query.from.id
        val message = query.message
        if (notFor(query)) {
            answerQuery(query, "Куда лезете? Это не вам!")
            return
        }
        if (Services.db.getChild(userId) != 0) {
            answerQuery(query, "У вас уже есть ребенок!")
            return
        }
        val childId = query.data.split(" ")[1].toInt()
        answerQuery(query, "Поздравляем! Теперь у вас ребенок")
        Methods.deleteMessage(message.chatId, message.messageId).call(handler)

        // user - inits, lover - accepts
        val user = TgUser(userId, query.from.firstName)
        val lover = TgUser(Methods.getChatMember(message.chatId, Services.db.getLover(userId)).call(handler).user)
        val child = TgUser(Methods.getChatMember(message.chatId, childId).call(handler).user)

        Services.db.setChild(user.id, lover.id, child.id)
        handler.sendMessage(
            child.id,
            "Поздравляем! Теперь ваши родители - ${user.link} и его супруг(а) ${lover.link}!"
        )
        val text =
            "Внимание все! Сегодня великий день усыновления ${user.link} и ${lover.link} ребёнка ${child.link}! Так давайте же поздравим их и съедим шавуху в часть такого праздника!"
        handler.sendMessage(message.chatId, text)
    }

}*/