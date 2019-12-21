package com.senderman

import org.telegram.telegrambots.meta.api.objects.Message

interface CommandExecutor {
    val command: String
    val desc: String
    val showInHelp: Boolean
        get() = true
    val forAllAdmins: Boolean
        get() = false
    val forPremium: Boolean
        get() = false
    val forMainAdmin: Boolean
        get() = false

    fun execute(message: Message)
}