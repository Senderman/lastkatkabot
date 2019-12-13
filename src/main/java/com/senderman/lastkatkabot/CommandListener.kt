package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.Command
import com.senderman.lastkatkabot.handlers.AdminHandler
import com.senderman.lastkatkabot.handlers.TournamentHandler
import com.senderman.lastkatkabot.handlers.UsercommandsHandler
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.Duel
import com.senderman.lastkatkabot.tempobjects.UserRow
import org.telegram.telegrambots.meta.api.objects.Message

internal class CommandListener constructor(
        private val handler: LastkatkaBotHandler,
        private val adminCommands: AdminHandler,
        private val tournamentHandler: TournamentHandler
) {

    private val usercommands = UsercommandsHandler(handler)

    @Command(name = "/action", desc = "сделать действие. Действие указывать чере пробел, можно реплаем")
    fun action(message: Message) = usercommands.action(message)

    @Command(name = "/f", desc = "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень")
    fun pressF(message: Message) = usercommands.pressF(message)

    @Command(name = "/cake", desc = "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней")
    fun cake(message: Message) = usercommands.cake(message)

    @Command(name = "/dice", desc = "бросить кубик. Можно указать диапазон, напр. /dice -5 9")
    fun dice(message: Message) = usercommands.dice(message)

    @Command(name = "/stats", desc = "статистика. Реплаем можно узнать статистику реплайнутого")
    fun stats(message: Message) = usercommands.stats(message)

    @Command(name = "/pinlist", desc = "ответьте этим на сообщение со списком игроков в верфульфа чтобы запинить его")
    fun pinlist(message: Message) = usercommands.pinList(message)

    @Command(name = "/getinfo", desc = "(reply) инфа о сообщении в формате JSON")
    fun getinfo(message: Message) = usercommands.getInfo(message)

    @Command(name = "/weather", desc = "погода. Если не указать город, то покажет погоду в последнем введенном вами городе")
    fun weather(message: Message) = usercommands.weather(message)

    @Command(name = "/feedback", desc = "написать разрабу. Что написать, пишите через пробел. Или просто реплайните")
    fun feedback(message: Message) = usercommands.feedback(message)

    @Command(name = "/top", desc = "топ игроков в Быки и Коровы")
    fun bncTop(message: Message) = usercommands.bncTop(message)

    @Command(name = "/help", desc = "помощь", showInHelp = false)
    fun help(message: Message) = usercommands.help(message)

    @Command(name = "/pair", desc = "пара дня")
    fun pair(message: Message) = usercommands.pair(message)

    @Command(name = "/lastpairs", desc = "последние 10 пар чата")
    fun lastpairs(message: Message) = usercommands.lastpairs(message)

    @Command(name = "/bnc", desc = "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа")
    fun bnc(message: Message) {
        if (message.chatId !in handler.bullsAndCowsGames)
            handler.bullsAndCowsGames[message.chatId] = BullsAndCowsGame(message)
        else handler.sendMessage(message.chatId, "В этом чате игра уже идет!")
    }

    @Command(name = "/duel", desc = "начать дуэль (мини-игра на рандом)")
    fun duel(message: Message) {
        if (message.isUserMessage) return
        val duel = Duel(message)
        handler.duels[duel.duelId] = duel
    }

    @Command(name = "/bnchelp", desc = "помощь по игре Быки и Коровы")
    fun bncHelp(message: Message) = usercommands.bncHelp(message)

    @Command(name = "/bncinfo", desc = "информация о текущей игре")
    fun bncInfo(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.sendGameInfo(message)
    }

    @Command(name = "/bncstop", desc = "запустить опрос об остановке игры")
    fun bncStop(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.createStopPoll(message)
    }

    @Command(name = "/bncruin", desc = "вкл/выкл режим антируина (когда все цифры известны)")
    fun bncRuin(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.changeAntiRuin()
    }

    @Command(name = "/badneko", desc = "(reply) добавить юзера в чс бота", forAllAdmins = true)
    fun badneko(message: Message) = adminCommands.addUser(message, DBService.UserType.BLACKLIST)

    @Command(name = "/goodneko", desc = "(reply) убрать юзера из чс бота", forAllAdmins = true)
    fun goodneko(message: Message) = adminCommands.goodneko(message)

    @Command(name = "/nekos", desc = "посмотреть чс бота. В лс работает как управление чс", forAllAdmins = true)
    fun nekos(message: Message) = adminCommands.listUsers(message, DBService.UserType.BLACKLIST)

    @Command(name = "/transfer", desc = "перенос стат юзера. /transfer fromId toId", forMainAdmin = true)
    fun transfer(message: Message) = adminCommands.transferStats(message)

    @Command(name = "/critical", desc = "очистка незакончившихся дуэлей", forAllAdmins = true)
    fun critical(message: Message) {
        handler.duels.clear()
        handler.sendMessage(message.chatId, "✅ Все неначатые дуэли были очищены!")
    }

    @Command(name = "/owners", desc = "управление/просмотр админами бота. Управление доступно только главному админу в лс", forAllAdmins = true)
    fun owners(message: Message) = adminCommands.listUsers(message, DBService.UserType.ADMINS)

    @Command(name = "/prem", desc = "управление/просмотр премиум-пользователями. Управление доступно только главному админу в лс", forAllAdmins = true)
    fun prem(message: Message) = adminCommands.listUsers(message, DBService.UserType.PREMIUM)

    @Command(name = "/setuphelp", desc = "инфо о команде /setup", forAllAdmins = true)
    fun setupHelp(message: Message) = adminCommands.setupHelp(message)

    @Command(name = "/score", desc = "name1 score1 name2 score2 - сообщить счет", forAllAdmins = true)
    fun score(message: Message) {
        if (!tournamentHandler.isEnabled) return
        tournamentHandler.score(message)
    }

    @Command(name = "/win", desc = "winner score loser score типСледующегоРаунда - завершить турнир (если текущий раунд - финал, используйте тип раунда over)", forAllAdmins = true)
    fun win(message: Message) {
        if (!tournamentHandler.isEnabled) return
        tournamentHandler.win(message)
    }

    @Command(name = "/rt", desc = "отменить турнир", forAllAdmins = true)
    fun resetTournament(message: Message) {
        if (!tournamentHandler.isEnabled) return
        tournamentHandler.resetTournament()
    }

    @Command(name = "/setup", desc = "настроить турнир", showInHelp = false, forAllAdmins = true)
    fun setup(message: Message) = tournamentHandler.setup(message)

    @Command(name = "/go", desc = "подтвердить данные", showInHelp = false, forAllAdmins = true)
    fun go(message: Message) = tournamentHandler.startTournament()

    @Command(name = "/ct", desc = "отменить введеные данные", showInHelp = false, forAllAdmins = true)
    fun ct(message: Message) = tournamentHandler.cancelSetup()

    @Command(name = "/tourmessage", desc = "(reply) главное сообщение турнира", forAllAdmins = true)
    fun tourMessage(message: Message) = tournamentHandler.tourmessage(message)

    @Command(name = "/owner", desc = "(reply) добавить админа бота", forMainAdmin = true)
    fun owner(message: Message) = adminCommands.addUser(message, DBService.UserType.ADMINS)

    @Command(name = "/addpremium", desc = "(reply) добавить премиум-пользователя", forMainAdmin = true)
    fun addPremium(message: Message) = adminCommands.addUser(message, DBService.UserType.PREMIUM)

    @Command(name = "/update", desc = "рассылка информации по обновлениям в чаты. Обновления писать построчно", forMainAdmin = true)
    fun update(message: Message) = adminCommands.update(message)

    @Command(name = "/announce", desc = "рассылка сообщения всем в личку", forMainAdmin = true)
    fun announce(message: Message) = adminCommands.announce(message)

    @Command(name = "/cc", desc = "очистка списка чатов от мусора и обновление названий", forMainAdmin = true)
    fun cleanChats(message: Message) = adminCommands.cleanChats()

    @Command(name = "/row", desc = "Рассчет юзеров, например няшек.\nСинтаксис: 1 строка - /row Список няшек\n" +
            "2 строка - няшка\n" +
            "3 строка - 5\n" +
            "(т.е. няшкой будет каждый пятый")
    fun row(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        try {
            handler.userRows[message.chatId] = UserRow(message)
        } catch (e: Exception) {
            handler.sendMessage(message.chatId, "Неверный формат!")
            return
        }
        Methods.deleteMessage(message.chatId, message.messageId).call(Services.handler)
    }

    @Command(name = "/getrow", desc = "Показать сообщение с рассчетом юзеров")
    fun getrow(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        if (message.chatId !in handler.userRows)
            handler.sendMessage(message.chatId, "У вас пока еще нет списка!")
        else
            handler.sendMessage(Methods.sendMessage(message.chatId, "Вот!")
                    .setReplyToMessageId(handler.userRows[message.chatId]?.messageId))
    }

    @Command(name = "/marryme", desc = "(reply) пожениться на ком-нибудь")
    fun marryme(message: Message) = usercommands.marryme(message)

    @Command(name = "/divorce", desc = "подать на развод")
    fun divorce(message: Message) = usercommands.divorce(message)

}