package com.senderman.anitrackerbot;

public class Services {

    public static final int MAX_ANIMES = 20;
    private static DBService db;

    public static void setDataBase(DBService db) {
        Services.db = db;
    }

    public static DBService db() {
        return db;
    }
}
