package com.senderman.lastkatkabot.feature.genshin.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("genshinUserInventoryItem")
public class GenshinUserInventoryItem {

    @Id
    private String id;
    private final long chatId;
    private final long userId;
    private final String itemId;
    private int amount;

    @Creator
    public GenshinUserInventoryItem(long chatId, long userId, String itemId, int amount) {
        this.id = generateId(chatId, userId, itemId);
        this.chatId = chatId;
        this.userId = userId;
        this.itemId = itemId;
        this.amount = amount;
    }

    private static String generateId(long chatId, long userId, String itemId) {
        return chatId + " " + userId + " " + itemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public long getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public void incAmount() {
        this.amount++;
    }
}
