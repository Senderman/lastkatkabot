package com.senderman.futurewars;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

class Player {

    int coins = 5;

    final int id;
    final String name;
    final int team;

    boolean isDead = false;
    boolean isReady = false;
    boolean pmReports = true;
    boolean usedClone = false;
    ACTION action = ACTION.NONE;

    int hp = 10;
    final Set<Player> attackers;
    int laser = 5;
    int shield = 5;
    int currentShield = 0;

    int rollCounter = 6;

    Player target;
    PlayerClone clone;
    Player(int id, String name, int team) {
        this.id = id;
        this.name = name.replace("<", "&lt;").replace(">", "&gt;");
        this.team = team;
        attackers = new HashSet<>();
    }

    int dmgTaken = 0;
    int afkTurns = 0;

    Message message;

    void prepareForNextTurn() {
        isReady = false;
        currentShield = 0;
        action = ACTION.NONE;
        target = null;
        message = null;
        dmgTaken = 0;
        attackers.clear();
        if (laser > 5)
            laser = 5;
        if (shield > 5)
            shield = 5;
        if (rollCounter < 6)
            rollCounter++;
    }

    enum ACTION {
        ATTACK, DEFENCE,
        CHARGE_LASER, CHARGE_SHIELD,
        DEF_FLAG, SUMMON_CLONE,
        ROLL, NONE
    }
}

class PlayerClone extends Player {

    private final Player origin;

    int turnsLeft = 4;

    PlayerClone(Player origin) {
        super(origin.id * -1, origin.name, origin.team);
        this.origin = origin;
        hp = 10;
        laser = origin.laser;
        shield = origin.shield;
        pmReports = false;
        isReady = true;
    }

    @Override
    void prepareForNextTurn() {
        super.prepareForNextTurn();
        isReady = true;
        turnsLeft--;
        origin.coins += coins;
        coins = 0;
    }

    void syncStats() {
        action = origin.action;
        laser = origin.laser;
        shield = origin.shield;
        currentShield = origin.currentShield;
        target = origin.target;
    }
}

class TeamFlag extends Player {

    TeamFlag(int team) {
        super(team * -1, "\uD83C\uDFF3️\u200D\uD83C\uDF08 Флаг команды " + team, team);
        hp = 15;
        pmReports = false;
        isReady = true;
        coins = 10;
    }

    @Override
    void prepareForNextTurn() {
        super.prepareForNextTurn();
        isReady = true;
        afkTurns = 0;
    }
}

class CoinMonster extends Player {

    CoinMonster(int id) {
        super(id, "\uD83D\uDC7B CoinMonster " + id, 228); // there're only 25 teams in the game, so team 228 for CoinMonsters
        hp = 2;
        coins = ThreadLocalRandom.current().nextInt(5, 10);
        pmReports = false;
        isReady = true;
    }

    @Override
    void prepareForNextTurn() {
        super.prepareForNextTurn();
        if (ThreadLocalRandom.current().nextInt(1) == 1)
            action = ACTION.ROLL;
        isReady = true;
        afkTurns = 0;
    }
}
