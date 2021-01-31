package com.senderman.lastkatkabot.usercommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class Help(
    private val handler: LastkatkaBotHandler,
    private val commands: Map<String, CommandExecutor>
) : CommandExecutor {

    override val command: String
        get() = "/help"
    override val desc: String
        get() = "помощь"
    override val showInHelp: Boolean
        get() = false

    override fun execute(message: Message) {
        val help =
            StringBuilder("Привет! Это очень полезный бот для проекта @lastkatka, который многое что умеет! Основные команды:\n\n")
        val adminHelp = StringBuilder("<b>Информация для админов бота</b>\n\n")
        val mainAdminHelp = StringBuilder("<b>Информация для главного админа бота</b>\n\n")
        val noobId = message.from.id

        for ((cmd, executor) in commands) {
            if (!executor.showInHelp)
                continue

            val helpLine = "$cmd - ${executor.desc}"
            if (noobId == Services.botConfig.mainAdmin && executor.forMainAdmin)
                mainAdminHelp.appendln(helpLine)
            else if (handler.isFromAdmin(message) && executor.forAllAdmins)
                adminHelp.appendln(helpLine)
            else if (!executor.forMainAdmin && !executor.forAllAdmins)
                help.appendln(helpLine)
            // TODO add help for premium users when needed
        }
        if (handler.isFromAdmin(message)) help.append("\n").append(adminHelp)
        if (noobId == Services.botConfig.mainAdmin) help.append("\n").append(mainAdminHelp)
        // attempt to send help to PM
        try {
            handler.execute(
                SendMessage(message.from.id.toLong(), help.toString()).enableHtml(true)
            )
        } catch (e: TelegramApiException) {
            handler.sendMessage(
                Methods.sendMessage(message.chatId, "Пожалуйста, начните диалог со мной в лс")
                    .setReplyToMessageId(message.messageId)
            )
            return
        }
        if (!message.isUserMessage) handler.sendMessage(
            Methods.sendMessage(message.chatId, "✅ Помощь была отправлена вам в лс")
                .setReplyToMessageId(message.messageId)
        )
    }

}