package com.senderman.lastkatkabot.callbacks

import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import com.senderman.neblib.TgUser
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

class AcceptChild(private val handler: LastkatkaBotHandler): CallbackHandler(handler) {
    override val trigger: String
        get() = Callbacks.ADOPT_CHILD


    /**
     * @param chatId - issuing chat's id
     * @param father - issuer of the request
     * @param mother - acceptor of the request
     * @param child - object of the request
     */
    private data class AdoptRequest(val chatId: Long, val father: TgUser, val mother: TgUser, val child: TgUser)
    private val checkQueue: Queue<AdoptRequest> = ConcurrentLinkedQueue()
    private var isCheckingRunning: Boolean = false

    override fun handle(query: CallbackQuery) {
        TODO()
        /*
        * checkQueue.offer(AdoptRequest(chatId, father, mother, child))
        Methods.sendMessage()
            .setChatId(chatId)
            .setText("Ваша заявка добавлена в очередь проверки на легальность усыновления. По окончанию Вы будете уведомлены о результате, в этом чатике")
            .setReplyToMessageId(message.messageId)
            .call(handler)

        if (!isCheckingRunning)
            runChecking()*/
    }

    // called by runChecking if no 1nc357 found and creates callback buttons
    private fun confirmAdoption(adoptRequest: AdoptRequest) {
        val (chatId, father, mother, child) = adoptRequest

    }


    //  called by runChecking if 1nc357 found
    private fun notifyCouldNotAdopt(adoptRequest: AdoptRequest) {
        val (chatId, father, mother, _) = adoptRequest
        handler.sendMessage(
            chatId,
            "${father.link}, ${mother.link}, Нелегальное усыновление, попахивающее инцестом! Осторожнее в следующий раз"
        )
    }

    // runs in separate thread and calls couldAdopt for each request. Shutdown at empty queue.
    private fun runChecking(): Unit = thread(false) {
        isCheckingRunning = true
        while (!checkQueue.isEmpty()) {
            val request = checkQueue.remove()
            val (_, father, mother, child) = request
            if (couldAdopt(father.id, child.id) && couldAdopt(mother.id, child.id))
                confirmAdoption(request)
            else
                notifyCouldNotAdopt(request)
        }
        isCheckingRunning = false
    }.start()

    // returns true if no 1nc357 found, else false
    private fun couldAdopt(parentId: Int, childId: Int): Boolean {
        var lChildId = childId
        var grandsonId: Int
        do {
            grandsonId = Services.db.getChild(lChildId)
            if (grandsonId == parentId) return false
            lChildId = grandsonId
        } while (grandsonId != 0)
        return true
    }

}