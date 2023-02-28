package com.senderman.lastkatkabot.feature.genshin.service;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinUserInventoryItem;

import java.util.List;

public interface GenshinUserInventoryItemService {

    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    GenshinUserInventoryItem findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

    GenshinUserInventoryItem save(GenshinUserInventoryItem item);

}
