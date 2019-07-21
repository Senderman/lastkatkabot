package com.senderman.lastkatkabot;

public class Services {

    private static LastkatkaBotHandler handler;

    private static DBService db;

    private static BotConfig botConfig;

    public static LastkatkaBotHandler handler() {
        return handler;
    }

    static void setHandler(LastkatkaBotHandler handler) {
        Services.handler = handler;
    }

    public static DBService db() {
        return db;
    }

    static void setDBService(DBService db) {
        Services.db = db;
    }

    public static BotConfig botConfig() {
        return botConfig;
    }

    static void setBotConfig(BotConfig config) {
        Services.botConfig = config;
    }
}