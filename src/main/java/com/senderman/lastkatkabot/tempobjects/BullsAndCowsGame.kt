package com.senderman.lastkatkabot.tempobjects

import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod
import com.senderman.TgUser
import com.senderman.lastkatkabot.LastkatkaBot
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class BullsAndCowsGame(message: Message) {
    private val chatId: Long
    private val startTime: Long
    private val answer: String
    private val history: StringBuilder
    private val messagesToDelete: MutableSet<Int>
    private val votedUsers: MutableSet<Int>
    private val checkedNumbers: MutableSet<String>
    private var length: Int
    private var attempts: Int
    private var voted: Int
    private var antiRuinEnabled: Boolean
    private val creator: TgUser

    init {
        val length: Int
        try {
            length = message.text.split(" ")[1].toInt()
            if (length < 4 || length > 10) this.length = 4 else this.length = length
        } catch (e: Exception) {
            this.length = 4
        }
        chatId = message.chatId
        creator = TgUser(message.from.id, message.from.firstName)
        attempts = (this.length * 2.5).toInt()
        voted = 0
        antiRuinEnabled = false
        history = StringBuilder()
        messagesToDelete = HashSet()
        votedUsers = HashSet()
        checkedNumbers = HashSet()

        gameMessage(chatId, "Генерируем число...")
        answer = generateRandom()
        startTime = System.currentTimeMillis()
        Services.db.saveBncGame(chatId, this)
        gameMessage(chatId, """
                |Число загадано! 
                |Отправляйте в чат ваши варианты, они должны состоять только из неповторяющихся чисел! 
                ||Правила игры - /bnchelp. 
                |Вкл/выкл режима антируина (когда все цифры известны) - /bncruin 
                |Просмотр хода игры - /bncinfo 
                |Остановить игру (голосование) - /bncstop)
                """.trimMargin())
    }

    fun check(message: Message) {
        val number = message.text
        if (number.length != length) return

        messagesToDelete.add(message.messageId)
        if (number == answer) {
            win(message)
            return
        }

        if (hasRepeatingDigits(number)) {
            gameMessage(chatId, "Загаданное число не может содержать повторяющиеся числа!")
            return
        }

        if (ruined(number)) {
            gameMessage(chatId, "Все правильные цифры уже известны, следите за игрой!")
            if (antiRuinEnabled) return
        }

        val result = calculate(number)
        if (number in checkedNumbers) {
            gameMessage(chatId, "$number - уже проверяли! ${result.bulls}Б ${result.cows}К")
            return
        }

        attempts--

        history.append(String.format("%1\$s - %2\$s: %3\$dБ %4\$dК\n",
                message.from.firstName, number, result.bulls, result.cows))

        if (attempts <= 0) {
            gameOver()
        } else {
            gameMessage(chatId, "$number ${result.bulls}Б ${result.cows}К, попыток: $attempts\n")
            checkedNumbers.add(number)
            Services.db.saveBncGame(chatId, this)
        }
    }

    fun sendGameInfo(message: Message) {
        messagesToDelete.add(message.messageId)
        val info = """
            |Длина числа: $length
            |Попыток осталось: $attempts
            |История: $history
            |Создатель игры: ${creator.getLink()}
            |Время игры: ${getSpentTime()}
        """.trimIndent()
        gameMessage(chatId, info)
    }

    fun changeAntiRuin() {
        antiRuinEnabled = !antiRuinEnabled
        val status = if (antiRuinEnabled) "Антируин включен!" else "Антируин выключен!"
        gameMessage(chatId, status)
    }

    fun createStopPoll(message: Message) {
        messagesToDelete.add(message.messageId)
        if (message.isUserMessage) { // who needs to vote in pm? :)
            gameOver()
            return
        }
        gameMessage(Methods.sendMessage()
                .setChatId(chatId)
                .setText(String.format("<b>Голосование за завершение игры</b>\n" +
                        "Осталось %1\$d голосов для завершения. Голос админа чата или создателя игры сразу заканчивает игру",
                        5 - voted))
                .setReplyMarkup(getEndgameMarkup())
                .setParseMode(ParseMode.HTML))
    }

    fun addVote(query: CallbackQuery) {
        if (query.from.id in votedUsers) {
            Methods.answerCallbackQuery()
                    .setText("Вы уже голосовали!")
                    .setShowAlert(true)
                    .setCallbackQueryId(query.id)
                    .call(Services.handler)
            return
        }
        voted++
        val user = Methods.getChatMember(chatId, query.from.id).call(Services.handler)
        if (voted == 5 || user.user.id == creator.id || user.status == "creator" || user.status == "administrator") {
            gameOver()
        }
        votedUsers.add(query.from.id)
        Methods.editMessageText()
                .setText(String.format("<b>Голосование за завершение игры</b>\n" +
                        "Осталось %1\$d голосов для завершения. Голос админа чата или создателя игры сразу заканчивает игру",
                        5 - voted))
                .setChatId(chatId)
                .setMessageId(query.message.messageId)
                .setReplyMarkup(getEndgameMarkup())
                .enableHtml()
                .call(Services.handler)
    }

    private fun ruined(number: String): Boolean {
        if (!antiRuinEnabled)
            return false

        for (i in 0 until length) {
            if (number[i] !in answer) {
                return true
            }
        }
        return false
    }

    private fun gameMessage(chatId: Long, text: String) {
        gameMessage(Methods.sendMessage(chatId, text))
    }

    private fun gameMessage(sm: SendMessageMethod) {
        messagesToDelete.add(Services.handler.sendMessage(sm).messageId)
    }

    private fun win(message: Message) {
        history.insert(0, String.format("%1\$s выиграл за %2\$d попыток! %3\$s - правильный ответ!\n\n",
                message.from.firstName, (length * 2.5 - (attempts - 1)).toInt(), answer))
        history.append("\nВот столько вы потратили времени: ${getSpentTime()}")
        Services.handler.sendMessage(chatId, history.toString())
        Services.db.incBNCWins(message.from.id, length)
        endGame()
    }

    private fun gameOver() {
        history.insert(0, String.format("Вы проиграли! Ответ: %1\$s\n\n", answer))
        history.append("\nВот столько вы потратили времени: ${getSpentTime()}")
        Services.handler.sendMessage(chatId, history.toString())
        endGame()
    }

    private fun endGame() {
        for (messageId in messagesToDelete) {
            Methods.deleteMessage(chatId, messageId).call(Services.handler)
        }
        Services.db.deleteBncGame(chatId)
        Services.handler.bullsAndCowsGames.remove(chatId)
    }

    private fun getSpentTime(): String {
        var timeSpent = System.currentTimeMillis() - startTime
        timeSpent /= 1000
        var sec = timeSpent
        var mins = sec / 60
        sec -= mins * 60
        val hours = mins / 60
        mins -= hours * 60
        return String.format("%02d:%02d:%02d", hours, mins, sec)
    }

    private fun generateRandom(): String {
        val random = IntArray(length)
        for (i in 0 until length) {
            do {
                random[i] = ThreadLocalRandom.current().nextInt(0, 10)
            } while (hasRepeatingDigits(random, i))
        }
        val sb = StringBuilder()
        for (i in random) sb.append(i)
        return sb.toString()
    }

    // check that array contains only unique numbers
    private fun hasRepeatingDigits(array: String): Boolean {
        for (i in 0 until length) {
            for (j in i + 1 until length) {
                if (array[i] == array[j]) {
                    return true
                }
            }
        }
        return false
    }

    // same as before but for an array under generating
    private fun hasRepeatingDigits(array: IntArray, genIndex: Int): Boolean {
        for (i in 0..genIndex) {
            for (j in i + 1..genIndex) {
                if (array[i] == array[j]) {
                    return true
                }
            }
        }
        return false
    }

    //calculate bulls and cows
    private fun calculate(player: String): Result {
        var bulls = 0
        var cows = 0
        for (i in 0 until length) {
            if (player[i] == answer[i]) {
                bulls++
            } else {
                for (j in 0 until length) {
                    if (player[i] == answer[j]) {
                        cows++
                    }
                }
            }
        }
        return Result(bulls, cows)
    }

    data class Result(var bulls: Int, var cows: Int)

    companion object {
        private fun getEndgameMarkup(): InlineKeyboardMarkup {
            val markup = InlineKeyboardMarkup()
            val rows = listOf(listOf(
                    InlineKeyboardButton()
                            .setText("Голосовать")
                            .setCallbackData(LastkatkaBot.CALLBACK_VOTE_BNC)))
            markup.keyboard = rows
            return markup
        }
    }
}