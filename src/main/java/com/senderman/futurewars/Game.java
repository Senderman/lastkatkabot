package com.senderman.futurewars;

import com.annimon.tgbotsmodule.api.methods.Methods;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.*;

class Game {
    private final FutureWarsHandler handler;
    private final long chatId;
    private final int messageId;
    private final JoinTimer joinTimer;
    private final ScheduledExecutorService scheduler;
    boolean chatReports = true;
    private int turnCounter = 1;
    private final Map<Integer, Set<Player>> teams;
    private boolean isStarted = false;
    private ScheduledFuture scheduledFuture;
    private final Map<Integer, Player> players;

    Game(long chatId, int messageId, JoinTimer joinTimer, FutureWarsHandler handler) {
        this.handler = handler;
        this.chatId = chatId;
        this.messageId = messageId;
        this.joinTimer = joinTimer;
        teams = new HashMap<>();
        players = new HashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    void start() {
        for (int team : teams.keySet()) { // create flags for all teams
            var flag = new TeamFlag(team);
            players.put(flag.id, flag);
        }

        teams.put(228, new HashSet<>()); // summon CoinMonsters
        for (int i = 0; i < 5; i++) {
            int monsterId;
            do {
                monsterId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
            } while (players.containsKey(monsterId));
            var monster = new CoinMonster(monsterId);
            players.put(monsterId, monster);
            teams.get(228).add(monster);
        }
        isStarted = true;
        for (Player player : players.values()) {
            if (player.id > 0 && player.team != 228)
                sendMainMenu(player.id);
        }
        scheduledFuture = scheduler.schedule(this::makeTurn, 1, TimeUnit.MINUTES);
    }

    private void makeTurn() {
        scheduledFuture.cancel(true);
        var result = new StringBuilder("Ход " + turnCounter + ":\n\n");
        var endOfResult = new StringBuilder("\nИтоги хода " + turnCounter + "\n\n");
        turnCounter++;

        for (int team : teams.keySet()) { // handle players actions

            if (!teamIsAlive(team))
                continue;

            result.append("<b>Команда ").append(team).append("</b>:\n");
            for (Player player : teams.get(team)) {

                if (player.isDead)
                    continue;

                if (!player.isReady) { // AFK
                    player.afkTurns++;
                    Methods.editMessageText()
                            .setChatId(player.id)
                            .setMessageId(player.message.getMessageId())
                            .setText("Время вышло!")
                            .setReplyMarkup(null)
                            .call(handler);
                    result.append(String.format("\uD83D\uDE34 %1$s пропускает ход\n", player.name));
                    continue;
                }

                switch (player.action) {
                    case ATTACK:
                        result.append(String.format("\uD83D\uDD34 %1$s стреляет в %2$s!\n",
                                player.name, player.target.name));
                        break;
                    case DEFENCE:
                        result.append(String.format("\uD83D\uDD35 %1$s заряжает щит на %2$d!\n",
                                player.name, player.currentShield));
                        break;
                    case DEF_FLAG:
                        result.append(String.format("\uD83D\uDEE1 %1$s блокирует %2$d урона флага!\n",
                                player.name, player.currentShield));
                        break;
                    case CHARGE_LASER:
                        result.append(String.format("\uD83D\uDD0B %1$s восстановил 3 ед. энергии лазера!\n",
                                player.name));
                        break;
                    case CHARGE_SHIELD:
                        result.append(String.format("\uD83D\uDD0B %1$s восстановил 3 ед. энергии щита!\n",
                                player.name));
                        break;
                    case ROLL:
                        result.append(String.format("\uD83D\uDC40 %1$s уворачивается!\n",
                                player.name));
                        break;
                    case SUMMON_CLONE:
                        var clone = new PlayerClone(player);
                        player.clone = clone;
                        player.usedClone = true;
                        teams.get(player.team).add(clone);
                        players.put(clone.id, clone);
                        result.append(String.format("\uD83D\uDE08 %1$s призывает клона!\n",
                                player.name));
                        break;
                }
            }
        }

        for (int team : teams.keySet()) { // handle results

            if (!teamIsAlive(team))
                continue;

            endOfResult.append("<b>Команда ").append(team).append("</b>:\n");

            for (Player player : teams.get(team)) {

                if (player.isDead)
                    continue;

                if (player.dmgTaken > 0) {

                    if (player.action != Player.ACTION.DEFENCE) {
                        if (player.action != Player.ACTION.ROLL) {
                            player.hp -= player.dmgTaken;
                            endOfResult.append(String.format("\uD83D\uDC94 %1$s теряет %2$d хп. У него остается %3$d хп!\n",
                                    player.name, player.dmgTaken, player.hp));
                        } else {
                            var chance = ThreadLocalRandom.current().nextInt(100);
                            if (chance > 60) { // chance to avoid attack is 60%
                                player.hp -= player.dmgTaken;
                                endOfResult.append(String.format("\uD83D\uDC94 %1$s теряет %2$d хп. У него остается %3$d хп!\n",
                                        player.name, player.dmgTaken, player.hp));
                            }
                        }
                    } else {
                        int gotEnergy;
                        if (player.dmgTaken <= player.currentShield) {
                            gotEnergy = 2;
                            endOfResult.append(String.format("\uD83D\uDC99 %1$s блокирует весь входящий урон", player.name));
                        } else {
                            gotEnergy = 1;
                            var hpLost = player.dmgTaken - player.currentShield;
                            player.hp -= hpLost;
                            endOfResult.append(String.format("\uD83D\uDC94 %1$s блокирует %2$d урона, теряет %3$d хп. У него остается %4$d хп!",
                                    player.name, player.currentShield, hpLost, player.hp));
                        }
                        player.laser += gotEnergy;
                        endOfResult.append(String.format(" а еще он восстановил %1$d энергии лазера!\n", gotEnergy));
                    }

                    if (player.hp <= 0) {
                        player.isDead = true;
                        int lootForEveryOne = player.coins / player.attackers.size() + 1;
                        for (Player killer : player.attackers) {
                            killer.coins += lootForEveryOne;
                        }
                        endOfResult.append(String.format("\uD83D\uDC80 %1$s умирает\n", player.name));
                    }
                }

                if (player.afkTurns == 2 && !player.isDead) { // death from AFK (and if he is still alive :)
                    player.isDead = true;
                    endOfResult.append(String.format("\uD83D\uDC80 %1$s умирает от АФК\n", player.name));
                }

                if (player.clone != null && player.clone.turnsLeft == 0) { // clone's timeout death
                    player.clone.isDead = true;
                    endOfResult.append(String.format("\uD83D\uDC80 Клон %1$s закончил свое существование\n", player.clone.name));
                    player.clone = null;
                    continue;
                }

                if (player.clone != null && player.isDead) { // clone cannot survive without owner
                    player.clone.isDead = true;
                    endOfResult.append(String.format("\uD83D\uDC80 Клон %1$s умирает без хозяина\n", player.clone.name));
                    continue;
                }

                player.prepareForNextTurn();

            }

            var flag = players.get(team * -1);
            if (flag != null) { // monsters does not have flag
                if (flag.dmgTaken > 0) {
                    flag.hp -= flag.dmgTaken;
                    endOfResult.append(String.format("\uD83D\uDC94 %1$s теряет %2$d хп. У него остается %3$d хп!\n",
                            flag.name, flag.dmgTaken, flag.hp));
                }
                if (flag.hp <= 0) {
                    int lootForEveryOne = flag.coins / flag.attackers.size() + 3;
                    for (Player killer : flag.attackers) {
                        killer.coins += lootForEveryOne;
                    }
                    for (Player player : teams.get(team)) {
                        player.isDead = true;
                    }
                    endOfResult.append(String.format("\uD83D\uDC80 Команда %1$d проиграла!\n", team));
                }
                flag.prepareForNextTurn();
            }
        }

        teams.get(228).removeIf(monster -> monster.isDead); // free memory from dead monsters
        players.values().removeIf(monster -> monster.isDead);

        int aliveTeams = 0;
        int winner = -1;
        for (int team : teams.keySet()) {
            if (team != 228 && teamIsAlive(team)) {
                aliveTeams++;
                winner = team;
            }
        }

        result.append(endOfResult);
        if (chatReports)
            handler.sendMessage(chatId, result.toString());
        for (Player player : players.values()) {
            if (player.pmReports)
                handler.sendMessage(player.id, result.toString());
        }

        if (aliveTeams == 1) {
            var winText = new StringBuilder();
            winText.append("\uD83D\uDC51 Команда ")
                    .append(winner)
                    .append(" победила! Выжившие:\n");
            for (Player player : teams.get(winner)) { // TODO stats for mongodb
                if (!player.isDead && player.id > 0)
                    winText.append("- ").append(player.name).append("\n");
            }

            handler.sendMessage(chatId, winText.toString());
            GameController.endgame(this);
        } else if (aliveTeams == 0) {
            handler.sendMessage(chatId, "\uD83D\uDC80 Все проиграли!");
            GameController.endgame(this);
        } else {
            if (turnCounter % 5 == 0) { // summon CoinMonsters
                while (teams.get(228).size() < 5) {
                    int monsterId;
                    do {
                        monsterId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
                    } while (players.containsKey(monsterId));
                    var monster = new CoinMonster(monsterId);
                    players.put(monsterId, monster);
                    teams.get(228).add(monster);
                }
            }
            scheduledFuture = scheduler.schedule(this::makeTurn, 1, TimeUnit.MINUTES); // next turn
            for (Player player : players.values()) {
                if (!player.isDead && player.id > 0 && player.team != 228)
                    sendMainMenu(player.id);
            }
        }

    }

    void doAction(int playerId, Player.ACTION action) {
        var player = players.get(playerId);
        if (player == null)
            return;

        player.action = action;
        var text = new StringBuilder("Ход " + turnCounter + ": ");

        switch (action) {
            case ATTACK:
                player.laser--;
                player.target.dmgTaken++;
                player.target.attackers.add(player);
                text.append("атака");
                if (player.clone != null) {
                    player.target.dmgTaken++;
                    player.target.attackers.add(player.clone);
                }
                break;
            case DEFENCE:
                player.shield -= player.currentShield;
                text.append("защита");
                break;
            case DEF_FLAG:
                players.get(player.team * -1).dmgTaken -= player.currentShield;
                player.shield -= player.currentShield;
                text.append("защита флага");
                if (player.clone != null)
                    players.get(player.team * -1).dmgTaken -= player.currentShield;
                break;
            case CHARGE_LASER:
                player.laser += 3;
                text.append("зарядка лазера");
                break;
            case CHARGE_SHIELD:
                player.shield += 3;
                text.append("зарядка щита");
                break;
            case ROLL:
                text.append("уворот");
                player.rollCounter = 0;
                break;
            case SUMMON_CLONE:
                text.append("призыв клона");
                break;
        }
        player.isReady = true;
        player.afkTurns = 0;
        if (player.clone != null && action != Player.ACTION.SUMMON_CLONE)
            player.clone.syncStats();

        Methods.editMessageText()
                .setChatId(playerId)
                .setMessageId(player.message.getMessageId())
                .setText(text.toString())
                .setReplyMarkup(null)
                .call(handler);
        check();
    }

    void setupShield(int playerId, Player.ACTION action) {
        var player = players.get(playerId);
        if (player == null)
            return;

        player.action = action;

        int[][] rowsArray = {
                {1, 2, 3, 4, 5},
                {-1, -2, -3, -4, -5}
        };
        var markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int[] row : rowsArray) {
            List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
            for (int value : row) {
                buttonsRow.add(new InlineKeyboardButton()
                        .setText(String.valueOf(value))
                        .setCallbackData(FutureWarsBot.CALLBACK_DEFENCE_VALUE + value + " " + chatId));
            }
            buttons.add(buttonsRow);
        }

        if (player.currentShield > 0) {
            var data = (action == Player.ACTION.DEFENCE) ?
                    FutureWarsBot.CALLBACK_CONFIRM_DEFENCE :
                    FutureWarsBot.CALLBACK_CONFIRM_FLAG_DEFENCE;
            buttons.add(List.of(new InlineKeyboardButton()
                    .setText("Защита")
                    .setCallbackData(data + chatId)));
        }

        buttons.add(List.of(new InlineKeyboardButton()
                .setText("Отмена")
                .setCallbackData(FutureWarsBot.CALLBACK_MAIN_MENU + chatId)));
        markup.setKeyboard(buttons);
        Methods.editMessageText()
                .setChatId(playerId)
                .setMessageId(players.get(playerId).message.getMessageId())
                .setText("Заряд щита: " + players.get(playerId).currentShield)
                .setReplyMarkup(markup)
                .call(handler);
    }

    void showTargets(int playerId) {
        var player = players.get(playerId);
        if (player == null)
            return;

        var markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Player target : players.values()) {
            if (target.team == player.team || target.isDead)
                continue;
            List<InlineKeyboardButton> row = List.of(new InlineKeyboardButton()
                    .setText(target.name)
                    .setCallbackData(FutureWarsBot.CALLBACK_ATTACK + target.id + " " + chatId));
            buttons.add(row);
        }
        buttons.add(List.of(new InlineKeyboardButton()
                .setText("Отмена")
                .setCallbackData(FutureWarsBot.CALLBACK_MAIN_MENU + chatId)));
        markup.setKeyboard(buttons);
        Methods.editMessageText()
                .setChatId(playerId)
                .setMessageId(player.message.getMessageId())
                .setText("Выберите игрока")
                .setReplyMarkup(markup)
                .call(handler);
    }

    void sendMainMenu(int playerId) {
        var player = players.get(playerId);
        if (player == null)
            return;

        player.currentShield = 0;

        var text = new StringBuilder("Ваша команда:\n\n");
        var flag = players.get(player.team * -1);
        text.append(String.format("%1$s: %2$d♥\n\n", flag.name, flag.hp));
        for (Player teammate : teams.get(player.team)) { // for each player's teammate
            if (teammate.isDead)
                continue;
            if (teammate.id == player.id) { // highlight current player's name and show coins
                text.append(String.format("<b>%1$s</b>: %2$d♥️ %3$d\uD83D\uDD34, %4$d\uD83D\uDD35, %5$d\uD83D\uDCB5\n",
                        player.name, player.hp, player.laser, player.shield, player.coins));
            } else if (teammate.id < 0) { // show clones
                if (teammate.id * -1 == playerId) { // higlight current player's clone
                    text.append(String.format("<b>%1$s (клон)</b>: %2$d♥️ %3$d\uD83D\uDD34, %4$d\uD83D\uDD35\n",
                            player.name, player.hp, player.laser, player.shield));
                } else {
                    text.append(String.format("%1$s (клон): %2$d♥️ %3$d\uD83D\uDD34, %4$d\uD83D\uDD35\n",
                            player.name, player.hp, player.laser, player.shield));
                }
            } else { // other players
                text.append(String.format("%1$s: %2$d♥️ %3$d\uD83D\uDD34, %4$d\uD83D\uDD35\n",
                        teammate.name, teammate.hp, teammate.laser, teammate.shield));
            }
        }
        var markup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        if (player.laser > 0)
            row1.add(new InlineKeyboardButton()
                    .setText("Атака")
                    .setCallbackData(FutureWarsBot.CALLBACK_SELECT_TARGET + chatId));
        if (player.shield > 0) {
            row1.add(new InlineKeyboardButton()
                    .setText("Защита")
                    .setCallbackData(FutureWarsBot.CALLBACK_DEFENCE + chatId));
            row1.add(new InlineKeyboardButton()
                    .setText("Защита флага")
                    .setCallbackData(FutureWarsBot.CALLBACK_FLAG_DEFENCE + chatId));
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(new InlineKeyboardButton()
                .setText("Зарядить лазер")
                .setCallbackData(FutureWarsBot.CALLBACK_CHARGE_LASER + chatId));
        row2.add(new InlineKeyboardButton()
                .setText("Зарядить щит")
                .setCallbackData(FutureWarsBot.CALLBACK_CHARGE_SHIELD + chatId));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        if (!player.usedClone) {
            row3.add(new InlineKeyboardButton()
                    .setText("Призвать клона")
                    .setCallbackData(FutureWarsBot.CALLBACK_SUMMON_CLONE + chatId));
        }
        if (player.rollCounter == 6) {
            row3.add(new InlineKeyboardButton()
                    .setText("Уворот")
                    .setCallbackData(FutureWarsBot.CALLBACK_ROLL + chatId));
        }

        var lastrow = List.of(new InlineKeyboardButton()
                .setText("Сообщение команде")
                .setSwitchInlineQueryCurrentChat(""));

        markup.setKeyboard(List.of(row1, row2, row3, lastrow));
        text.append("\nВыберите действие:");
        if (player.message == null) {
            player.message = handler.sendMessage(Methods.sendMessage()
                    .setChatId(player.id)
                    .setText(text.toString())
                    .setParseMode(ParseMode.HTML)
                    .setReplyMarkup(markup));
        } else {
            Methods.editMessageText()
                    .setChatId(player.id)
                    .setMessageId(player.message.getMessageId())
                    .setText(text.toString())
                    .setParseMode(ParseMode.HTML)
                    .setReplyMarkup(markup)
                    .call(handler);
        }
    }

    void setTarget(int playerId, int targetId) {
        var player = players.get(playerId);
        if (player == null)
            return;

        player.target = players.get(targetId);
    }

    private void check() { // end turn before time's up
        for (Player player : players.values()) {
            if (!player.isDead && !player.isReady) {
                return;
            }
        }
        makeTurn();
    }

    private boolean teamIsAlive(int team) {
        for (Player player : teams.get(team)) {
            if (!player.isDead) {
                return true;
            }
        }
        return false;
    }

    ScheduledExecutorService getTurnTimer() {
        return scheduler;
    }

    JoinTimer getJoinTimer() {
        return joinTimer;
    }

    long getChatId() {
        return chatId;
    }

    int getMessageId() {
        return messageId;
    }

    boolean isStarted() {
        return isStarted;
    }

    Map<Integer, Set<Player>> getTeams() {
        return teams;
    }

    Map<Integer, Player> getPlayers() {
        return players;
    }
}
