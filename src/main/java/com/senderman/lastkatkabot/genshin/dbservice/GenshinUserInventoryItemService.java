package com.senderman.lastkatkabot.genshin.dbservice;

import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;

import java.util.List;

public interface GenshinUserInventoryItemService {

    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    GenshinUserInventoryItem findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

    GenshinUserInventoryItem save(GenshinUserInventoryItem item);

}
