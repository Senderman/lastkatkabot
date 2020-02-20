package com.senderman.lastkatkabot.admincommands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CleanChats : CommandExecutor {

    override val forMainAdmin: Boolean
        get() = true
    override val command: String
        get() = "/cc"
    override val desc: String
        get() = "очистка списка чатов от мусора и обновление названий"

    override fun execute(message: Message) {
        cleanChats()
    }

    companion object {
        fun cleanChats() {
            Services.handler.sendMessage(Services.botConfig.mainAdmin, "Очистка чатов...")
            val chats = Services.db.getChatTitleMap()
            val cores = Runtime.getRuntime().availableProcessors()
            val executor = Executors.newFixedThreadPool(cores)
            val futures = executor.invokeAll(splitCleanupTasks(cores, chats))
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.MINUTES)
            Services.db.cleanup()
            var totalChatsDeleted = 0
            for (future in futures)
                totalChatsDeleted += future.get()

            Services.handler.sendMessage(
                Services.botConfig.mainAdmin,
                "Чаты обновлены! Удалено $totalChatsDeleted чатов"
            )
        }

        private fun splitCleanupTasks(workers: Int, chats: Map<Long, String>): List<Callable<Int>> {
            val partSize = chats.size / workers
            val list = ArrayList<Callable<Int>>()
            for (i in 0 until workers - 1) {
                list.add(cleanupTask(i * partSize, (i + 1) * partSize, chats))
            }
            list.add(cleanupTask((workers - 1) * partSize, chats.size, chats))
            return list
        }

        private fun cleanupTask(start: Int, bound: Int, chats: Map<Long, String>): Callable<Int> {
            return Callable {
                var deletedChats = 0
                val keys = chats.keys.toList()
                for (i in start until bound) {
                    val chatId = keys[i]
                    try {
                        val sendAction = SendChatAction()
                            .setChatId(chatId)
                            .setAction(ActionType.TYPING)
                        Services.handler.execute(sendAction)
                    } catch (e: Exception) {
                        deletedChats++
                        Services.db.removeChat(chatId)
                        Methods.leaveChat(chatId).call(Services.handler)
                    }
                }
                deletedChats
            }
        }
    }
}


        
