package com.senderman.lastkatkabot.feature.genshin.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.*;

@MappedEntity("genshin_user_inventory_item")
public class GenshinUserInventoryItem {

    @EmbeddedId
    private final PrimaryKey primaryKey;

    @MappedProperty("amount")
    private int amount;

    public GenshinUserInventoryItem(long chatId, long userId, String itemId, int amount) {
        this.primaryKey = new PrimaryKey(chatId, userId, itemId);
        this.amount = amount;
    }

    @Creator
    public GenshinUserInventoryItem(PrimaryKey primaryKey, int amount) {
        this.primaryKey = primaryKey;
        this.amount = amount;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @Transient
    public String getItemId() {
        return primaryKey.getItemId();
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void incAmount() {
        this.amount++;
    }

    @Embeddable
    public static class PrimaryKey {

        @MappedProperty("chat_id")
        private final long chatId;

        @MappedProperty("user_id")
        private final long userId;

        @MappedProperty("item_id")
        private final String itemId;

        public PrimaryKey(long chatId, long userId, String itemId) {
            this.chatId = chatId;
            this.userId = userId;
            this.itemId = itemId;
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
    }
}
