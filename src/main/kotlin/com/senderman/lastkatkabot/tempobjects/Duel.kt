package com.senderman.lastkatkabot.tempobjects

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.Services
import com.senderman.lastkatkabot.callbacks.Callbacks
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.concurrent.ThreadLocalRandom

class Duel(message: Message) {
    private val chatId: Long = message.chatId
    private val player1: TgUser = TgUser(message.from)
    private lateinit var player2: TgUser
    private val messageId: Int
    val duelId: String

    init {
        val sm = Methods.sendMessage()
            .setChatId(chatId)
            .setText("\uD83C\uDFAF Набор на дуэль! Жмите кнопку ниже\nДжойнулись:\n${player1.name}")
            .setReplyMarkup(getDuelReplyMarkup())
        messageId = Services.handler.sendMessage(sm).messageId
        duelId = "$chatId $messageId"
    }


    fun join(query: CallbackQuery) {
        if (this::player2.isInitialized) {
            answerCallbackQuery(query, "\uD83D\uDEAB Дуэлянтов уже набрали, увы", true)
            return
        }
        if (query.from.id == player1.id) {
            answerCallbackQuery(
                query,
                "\uD83D\uDC7A Я думаю, что тебе стоит сходить к психологу! Ты вызываешь на дуэль самого себя",
                true
            )
            return
        }
        player2 = TgUser(query.from.id, query.from.firstName)
        answerCallbackQuery(query, "✅ Вы успешно присоединились к дуэли!", false)
        start()
    }

    private fun start() {
        val randomInt = ThreadLocalRandom.current().nextInt(100)
        val winner = if (randomInt < 50) player1 else player2
        val loser = if (randomInt < 50) player2 else player1
        val winnerName = winner.name
        val loserName = loser.name

        var duelResult = """
                <b>Дуэль</b>
                ${player1.name} vs ${player2.name}
                
                Противники разошлись в разные стороны, развернулись лицом друг к другу, и $winnerName выстрелил первым!
                $loserName лежит на земле, истекая кровью!
                """.trimIndent() + "\n"

        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            duelResult += """
                   Но, умирая, $loserName успевает выстрелить в голову $winnerName!
                    $winnerName падает замертво!
                   💀 <b>Дуэль окончилась ничьей!</b>
                   """.trimIndent()
            Services.db.incTotalDuels(winner.id)
            Services.db.incTotalDuels(loser.id)
        } else {
            duelResult += "\uD83D\uDC51 <b>$winnerName выиграл дуэль!</b>"
            Services.db.incDuelWins(winner.id)
            Services.db.incTotalDuels(loser.id)
        }

        Methods.editMessageText()
            .setChatId(chatId)
            .setMessageId(messageId)
            .setText(duelResult)
            .setParseMode(ParseMode.HTML)
            .call(Services.handler)
        Services.handler.duels.remove(duelId)
    }

    companion object {
        fun answerCallbackQuery(query: CallbackQuery, text: String?, showAsAlert: Boolean) {
            Methods.answerCallbackQuery()
                .setText(text)
                .setCallbackQueryId(query.id)
                .setShowAlert(showAsAlert)
                .call(Services.handler)
        }

        private fun getDuelReplyMarkup(): InlineKeyboardMarkup {
            val markup = InlineKeyboardMarkup()
            markup.keyboard = listOf(
                listOf(
                    InlineKeyboardButton()
                        .setText("Присоединиться")
                        .setCallbackData(Callbacks.JOIN_DUEL)
                )
            )
            return markup
        }
    }
}