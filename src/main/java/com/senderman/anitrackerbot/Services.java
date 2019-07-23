package com.senderman.anitrackerbot;

public class Services {

    private static DBService db;

    public static void setDataBase(DBService db) {
        Services.db = db;
    }

    public static DBService db() {
        return db;
    }
}
