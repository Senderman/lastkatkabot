package com.senderman.lastkatkabot.model;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.util.List;
import java.util.Set;

@MappedEntity("chatInfo")
public class ChatInfo {

    @Id
    private final long chatId;
    @Nullable
    private List<String> lastPairs;
    @Nullable
    private Integer lastPairDate;
    @Nullable
    private Set<String> forbiddenCommands;
    @Nullable
    private String greetingStickerId;

    @Creator
    public ChatInfo(long chatId) {
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
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
