package com.senderman.lastkatkabot.feature.chatsettings.model;

import com.senderman.lastkatkabot.util.convert.StringListAttributeConverter;
import com.senderman.lastkatkabot.util.convert.StringSetAttributeConverter;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.util.List;
import java.util.Set;

@MappedEntity("CHAT_INFO")
public class ChatInfo {

    @Id
    @MappedProperty("chat_id")
    private final long chatId;

    @Nullable
    @MappedProperty(value = "last_pairs", converter = StringListAttributeConverter.class)
    private List<String> lastPairs;

    @Nullable
    @MappedProperty("last_pair_date")
    private Integer lastPairDate;

    @Nullable
    @MappedProperty(value = "forbidden_commands", converter = StringSetAttributeConverter.class)
    private Set<String> forbiddenCommands;

    @Nullable
    @MappedProperty("greeting_sticker_id")
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
