package com.senderman.lastkatkabot.genshin.model;


@TypeAlias("genshinUserInventoryItem")
public class GenshinUserInventoryItem {

    @Id
    private String id;
    private long chatId;
    private long userId;
    private String itemId;
    private int amount;

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

    public String getId() {
        return id;
    }
}
