package com.senderman.lastkatkabot.callbacks

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.api.methods.Methods
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

abstract class CallbackHandler(private val botHandler: BotHandler) {

    abstract val trigger: String // characters before the first space in query's data
    abstract fun handle(query: CallbackQuery)

    protected fun CallbackQuery.getCleanData() = this.data.split(" ", limit = 2)[1].trim() // data w/o trigger

    protected fun answerQuery(query: CallbackQuery, text: String, showAlert: Boolean = true) {
        Methods.answerCallbackQuery()
            .setCallbackQueryId(query.id)
            .setText(text)
            .setShowAlert(showAlert)
            .call(botHandler)
    }

    protected fun editText(query: CallbackQuery, text: String, markup: InlineKeyboardMarkup? = null) {
        editText(query.message.chatId, query.message.messageId, text, markup)
    }

    private fun editText(chatId: Long, messageId: Int, text: String, markup: InlineKeyboardMarkup? = null) {
        Methods.editMessageText()
            .setChatId(chatId)
            .setMessageId(messageId)
            .setText(text)
            .setReplyMarkup(markup)
            .call(botHandler)
    }

}