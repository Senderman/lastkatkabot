package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.Command;
import com.senderman.lastkatkabot.handlers.AdminHandler;
import com.senderman.lastkatkabot.handlers.TournamentHandler;
import com.senderman.lastkatkabot.handlers.UsercommandsHandler;
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.tempobjects.Duel;
import com.senderman.lastkatkabot.tempobjects.UserRow;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CommandListener {
    private final LastkatkaBotHandler handler;
    private final UsercommandsHandler usercommands;
    private final AdminHandler adminCommands;
    private final TournamentHandler tournamentHandler;

    CommandListener(LastkatkaBotHandler handler) {
        this.handler = handler;
        usercommands = new UsercommandsHandler(handler);
        adminCommands = new AdminHandler(handler);
        tournamentHandler = handler.tournamentHandler;
    }

    @Command(name = "/action", desc = "сделать действие. Действие указывать чере пробел, можно реплаем")
    public void action(Message message) {
        usercommands.action(message);
    }

    @Command(name = "/f", desc = "(reply) press f to pay respects. А можно вот так: /f штаны за 40 хривень")
    public void pressF(Message message) {
        usercommands.pressF(message);
    }

    @Command(name = "/cake", desc = "(reply) подарить тортик. Можно указать начинку, напр. /cake с вишней")
    public void cake(Message message) {
        usercommands.cake(message);
    }

    @Command(name = "/dice", desc = "бросить кубик. Можно указать диапазон, напр. /dice -5 9")
    public void dice(Message message) {
        usercommands.dice(message);
    }

    @Command(name = "/stats", desc = "статистика. Реплаем можно узнать статистику реплайнутого")
    public void stats(Message message) {
        usercommands.stats(message);
    }

    @Command(name = "/pinlist", desc = "ответьте этим на сообщение со списком игроков в верфульфа чтобы запинить его")
    public void pinlist(Message message) {
        usercommands.pinlist(message);
    }

    @Command(name = "/getinfo", desc = "(reply) инфа о сообщении в формате JSON")
    public void getinfo(Message message) {
        usercommands.getinfo(message);
    }

    @Command(name = "/weather", desc = "погода. Если не указать город, то покажет погоду в последнем введенном вами городе")
    public void weather(Message message) {
        usercommands.weather(message);
    }

    @Command(name = "/feedback", desc = "написать разрабу. Что написать, пишите через пробел")
    public void feedback(Message message) {
        usercommands.feedback(message);
    }

    @Command(name = "/top", desc = "топ игроков в Быки и Коровы")
    public void bncTop(Message message) {
        usercommands.bncTop(message);
    }

    @Command(name = "/help", desc = "помощь", showInHelp = false)
    public void help(Message message) {
        usercommands.help(message);
    }

    @Command(name = "/pair", desc = "пара дня")
    public void pair(Message message) {
        usercommands.pair(message);
    }

    @Command(name = "/lastpairs", desc = "последние 10 пар чата")
    public void lastpairs(Message message) {
        usercommands.lastpairs(message);
    }

    @Command(name = "/bnc", desc = "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа")
    public void bnc(Message message) {
        if (!handler.bullsAndCowsGames.containsKey(message.getChatId()))
            handler.bullsAndCowsGames.put(message.getChatId(), new BullsAndCowsGame(message));
        else
            handler.sendMessage(message.getChatId(), "В этом чате игра уже идет!");
    }

    @Command(name = "/duel", desc = "начать дуэль (мини-игра на рандом)")
    public void duel(Message message) {
        if (message.isUserMessage())
            return;
        var duel = new Duel(message);
        handler.duels.put(duel.getDuelId(), duel);
    }

    @Command(name = "/bnchelp", desc = "помощь по игре Быки и Коровы")
    public void bncHelp(Message message) {
        usercommands.bncHelp(message);
    }

    @Command(name = "/bncinfo", desc = "информация о текущей игре")
    public void bncInfo(Message message) {
        if (handler.bullsAndCowsGames.containsKey(message.getChatId()))
            handler.bullsAndCowsGames.get(message.getChatId()).sendGameInfo(message);
    }

    @Command(name = "/bncstop", desc = "запустить опрос об остановке игры")
    public void bncStop(Message message) {
        if (handler.bullsAndCowsGames.containsKey(message.getChatId()))
            handler.bullsAndCowsGames.get(message.getChatId()).createStopPoll(message);
    }

    @Command(name = "/bncruin", desc = "вкл/выкл режим антируина (когда все цифры известны)")
    public void bncRuin(Message message) {
        if (handler.bullsAndCowsGames.containsKey(message.getChatId()))
            handler.bullsAndCowsGames.get(message.getChatId()).changeAntiRuin();
    }

    @Command(name = "/badneko", desc = "(reply) добавить юзера в чс бота", forAllAdmins = true)
    public void badneko(Message message) {
        adminCommands.addUser(message, DBService.COLLECTION_TYPE.BLACKLIST);
    }

    @Command(name = "/goodneko", desc = "(reply) убрать юзера из чс бота", forAllAdmins = true)
    public void goodneko(Message message) {
        adminCommands.goodneko(message);
    }

    @Command(name = "/nekos", desc = "посмотреть чс бота. В лс работает как управление чс", forAllAdmins = true)
    public void nekos(Message message) {
        adminCommands.listUsers(message, DBService.COLLECTION_TYPE.BLACKLIST);
    }

    @Command(name = "/critical", desc = "очистка незакончившихся дуэлей", forAllAdmins = true)
    public void critical(Message message) {
        handler.duels.clear();
        handler.sendMessage(message.getChatId(), "✅ Все неначатые дуэли были очищены!");
    }

    @Command(name = "/owners", desc = "управление/просмотр админами бота. Управление доступно только главному админу в лс", forAllAdmins = true)
    public void owners(Message message) {
        adminCommands.listUsers(message, DBService.COLLECTION_TYPE.ADMINS);
    }

    @Command(name = "/prem", desc = "управление/просмотр премиум-пользователями. Управление доступно только главному админу в лс", forAllAdmins = true)
    public void prem(Message message) {
        adminCommands.listUsers(message, DBService.COLLECTION_TYPE.PREMIUM);
    }

    @Command(name = "/setuphelp", desc = "инфо о команде /setup", forAllAdmins = true)
    public void setupHelp(Message message) {
        adminCommands.setupHelp(message);
    }

    @Command(name = "/score", desc = "name1 score1 name2 score2 - сообщить счет", forAllAdmins = true)
    public void score(Message message) {
        if (!tournamentHandler.isEnabled)
            return;
        tournamentHandler.score(message);
    }

    @Command(name = "/win",
            desc = "winner score loser score типСледующегоРаунда - завершить турнир (если текущий раунд - финал, используйте тип раунда over)",
            forAllAdmins = true)
    public void win(Message message) {
        if (!tournamentHandler.isEnabled)
            return;
        tournamentHandler.win(message);
    }

    @Command(name = "/rt", desc = "отменить турнир", forAllAdmins = true)
    public void resetTournament(Message message) {
        if (!tournamentHandler.isEnabled)
            return;
        tournamentHandler.resetTournament();
    }

    @Command(name = "/setup", desc = "настроить турнир", showInHelp = false, forAllAdmins = true)
    public void setup(Message message) {
        tournamentHandler.setup(message);
    }

    @Command(name = "/go", desc = "подтвердить данные", showInHelp = false, forAllAdmins = true)
    public void go(Message message) {
        tournamentHandler.startTournament();
    }

    @Command(name = "/ct", desc = "отменить введеные данные", showInHelp = false, forAllAdmins = true)
    public void ct(Message message) {
        tournamentHandler.cancelSetup();
    }

    @Command(name = "/tourmessage", desc = "(reply) главное сообщение турнира", forAllAdmins = true)
    public void tourMessage(Message message) {
        tournamentHandler.tourmessage(message);
    }

    @Command(name = "/owner", desc = "(reply) добавить админа бота", forMainAdmin = true)
    public void owner(Message message) {
        adminCommands.addUser(message, DBService.COLLECTION_TYPE.ADMINS);
    }

    @Command(name = "/addpremium", desc = "(reply) добавить премиум-пользователя", forMainAdmin = true)
    public void addPremium(Message message) {
        adminCommands.addUser(message, DBService.COLLECTION_TYPE.PREMIUM);
    }

    @Command(name = "/update", desc = "рассылка информации по обновлениям в чаты. Обновления писать построчно", forMainAdmin = true)
    public void update(Message message) {
        adminCommands.update(message);
    }

    @Command(name = "/announce", desc = "рассылка сообщения всем в личку", forMainAdmin = true)
    public void announce(Message message) {
        adminCommands.announce(message);
    }

    @Command(name = "/chats", desc = "управление чатами", forMainAdmin = true)
    public void chats(Message message) {
        adminCommands.chats(message);
    }

    @Command(name = "/cc", desc = "очистка списка чатов от мусора и обновление названий", forMainAdmin = true)
    public void cleanChats(Message message) {
        adminCommands.cleanChats(message);
    }

    @Command(name = "/raven", desc = "Стата по сообщениям равен", forPremium = true, showInHelp = false)
    public void raven(Message message) {
        handler.sendMessage(message.getChatId(),
                "Самая долгая переписка Равен и Жамы в котомафии - " + Services.db().getRavenRecord() + " сообщений подряд");
    }

    @Command(name = "/row",
            desc = "Рассчет юзеров, например няшек. Синтаксис: 1 строка - /row Список няшек\n" +
                    "2 строка - няшка\n" +
                    "3 строка - 5\n" +
                    "(т.е. няшкой будет каждый пятый")
    public void row(Message message) {
        if (!message.isGroupMessage() && !message.isSuperGroupMessage())
            return;

        UserRow oldRow = null;
        if (handler.userRows.containsKey(message.getChatId()))
            oldRow = handler.userRows.get(message.getChatId());
        try {
            handler.userRows.put(message.getChatId(), new UserRow(message));
        } catch (Exception e) {
            handler.sendMessage(message.getChatId(), "Неверный формат!");
            return;
        }
        if (oldRow != null)
            handler.userRows.remove(message.getChatId(), oldRow);
        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(Services.handler());
    }

    @Command(name = "/getrow", desc = "Показать сообщение с рассчетом юзеров")
    public void getrow(Message message) {
        if (!message.isGroupMessage() && !message.isSuperGroupMessage())
            return;
        if (!handler.userRows.containsKey(message.getChatId())) {
            handler.sendMessage(message.getChatId(), "У вас пока еще нет списка!");
            return;
        }
        handler.sendMessage(Methods.sendMessage(message.getChatId(), "Вот!")
                .setReplyToMessageId(handler.userRows.get(message.getChatId()).getMessageId()));
    }
}
