package com.senderman.lastkatkabot.genshin.dbservice.mongodb;

import com.senderman.lastkatkabot.genshin.dbservice.GenshinUserInventoryItemService;
import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;
import com.senderman.lastkatkabot.genshin.repository.GenshinUserInventoryItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoGenshinUserInventoryItemService implements GenshinUserInventoryItemService {

    private final GenshinUserInventoryItemRepository repo;

    public MongoGenshinUserInventoryItemService(GenshinUserInventoryItemRepository repo) {
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
        return repo.save(item);
    }
}
