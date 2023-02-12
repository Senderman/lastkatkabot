package com.senderman.lastkatkabot.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@TypeAlias("chatinfo")
public class ChatInfo {

    @Id
    private long chatId;
    @Nullable
    private List<String> lastPairs;
    @Nullable
    private Integer lastPairDate;
    @Nullable
    private Set<String> forbiddenCommands;
    @Nullable
    private String greetingStickerId;

    public ChatInfo(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public @Nullable List<String> getLastPairs() {
        return lastPairs;
    }

    public void setLastPairs(@Nullable List<String> lastPairs) {
        this.lastPairs = lastPairs;
    }

    public @Nullable Integer getLastPairDate() {
        return lastPairDate;
    }

    public void setLastPairDate(@Nullable Integer lastPairDate) {
        this.lastPairDate = lastPairDate;
    }

    public @Nullable Set<String> getForbiddenCommands() {
        return forbiddenCommands;
    }

    public void setForbiddenCommands(@Nullable Set<String> forbiddenCommands) {
        this.forbiddenCommands = forbiddenCommands;
    }

    public @Nullable String getGreetingStickerId() {
        return greetingStickerId;
    }

    public void setGreetingStickerId(@Nullable String greetingStickerId) {
        this.greetingStickerId = greetingStickerId;
    }
}
