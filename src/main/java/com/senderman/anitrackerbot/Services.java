package com.senderman.anitrackerbot;

public class Services {

    private static DBService db;

    private static BotConfig botConfig;

    static void setDataBase(DBService db) {
        Services.db = db;
    }

    public static DBService db() {
        return db;
    }

    public static void setBotConfig(BotConfig botConfig) {
        Services.botConfig = botConfig;
    }

    public static BotConfig config() {
        return botConfig;
    }
}
