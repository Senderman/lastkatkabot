package com.senderman.lastkatkabot.util;

public class DbCleanupResults {
    private final long users;
    private final long chats;
    private final long bncGames;
    private final long marriageRequests;

    public DbCleanupResults(long users, long chats, long bncGames, long marriageRequests) {
        this.users = users;
        this.chats = chats;
        this.bncGames = bncGames;
        this.marriageRequests = marriageRequests;
    }

    public long getUsers() {
        return users;
    }

    public long getChats() {
        return chats;
    }

    public long getBncGames() {
        return bncGames;
    }

    public long getMarriageRequests() {
        return marriageRequests;
    }
}
