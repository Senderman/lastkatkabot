package com.senderman.lastkatkabot.genshin.model;

import org.springframework.data.annotation.TypeAlias;

@TypeAlias("genshinUserInventoryItem")
public class GenshinUserInventoryItem {

    private long chatId;
    private long userId;
    private String itemId;
    private int amount;

    public GenshinUserInventoryItem() {
    }

    public GenshinUserInventoryItem(long chatId, long userId, String itemId, int amount) {
        this.chatId = chatId;
        this.userId = userId;
        this.itemId = itemId;
        this.amount = amount;
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
        amount++;
    }


}
