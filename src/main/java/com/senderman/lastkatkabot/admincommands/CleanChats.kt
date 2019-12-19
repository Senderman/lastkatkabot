package com.senderman.lastkatkabot.admincommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CleanChats constructor(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forMainAdmin: Boolean
        get() = true
    override val command: String
        get() = "/cc"
    override val desc: String
        get() = "очистка списка чатов от мусора и обновление названий"

    override fun execute(message: Message) {
        handler.sendMessage(Services.botConfig.mainAdmin, "Очистка чатов...")
        val chats = Services.db.getChatTitleMap()
        val cores = Runtime.getRuntime().availableProcessors()
        val executor = Executors.newFixedThreadPool(cores)
        executor.invokeAll(splitCleanupTasks(cores, chats))
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.MINUTES)
        Services.db.cleanup()
        handler.sendMessage(Services.botConfig.mainAdmin, "Чаты обновлены!")
    }

    private fun splitCleanupTasks(workers: Int, chats: Map<Long, String>): List<Callable<Unit>> {
        val partSize = chats.size / workers
        val list = ArrayList<Callable<Unit>>()
        for (i in 0 until workers - 1) {
            list.add(cleanupTask(i * partSize, (i + 1) * partSize, chats))
        }
        list.add(cleanupTask((workers - 1) * partSize, chats.size, chats))
        return list
    }

    private fun cleanupTask(start: Int, bound: Int, chats: Map<Long, String>): Callable<Unit> {
        return Callable {
            val keys = chats.keys.toList()
            for (i in start until bound) {
                val chatId = keys[i]
                try {
                    val chat = Methods.getChat(chatId).call(handler)
                    val title = chat.title
                    Services.db.updateTitle(chatId, title)
                } catch (e: Exception) {
                    Services.db.removeChat(chatId)
                    Methods.leaveChat(chatId).call(handler)
                    handler.sendMessage(Services.botConfig.mainAdmin, "Чат \"${chats[chatId]}\" удален из списка!")
                }
            }
        }
    }
}


        
