package com.senderman.lastkatkabot.feature.genshin.service;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinUserInventoryItem;
import com.senderman.lastkatkabot.feature.genshin.repository.GenshinUserInventoryItemRepository;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class PgsqlGenshinUserInventoryItemService implements GenshinUserInventoryItemService {

    private final GenshinUserInventoryItemRepository repo;

    public PgsqlGenshinUserInventoryItemService(GenshinUserInventoryItemRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId) {
        return repo.findByChatIdAndUserId(chatId, userId);
    }

    @Override
    public GenshinUserInventoryItem findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId) {
        return repo.findByChatIdAndUserIdAndItemId(chatId, userId, itemId).orElseGet(
                () -> new GenshinUserInventoryItem(chatId, userId, itemId, 0)
        );
    }

    @Override
    public GenshinUserInventoryItem save(GenshinUserInventoryItem item) {
        return repo.existsById(item.getPrimaryKey()) ? repo.update(item) : repo.save(item);
    }
}
