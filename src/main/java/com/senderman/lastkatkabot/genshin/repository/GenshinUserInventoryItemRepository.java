package com.senderman.lastkatkabot.genshin.repository;

import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenshinUserInventoryItemRepository extends CrudRepository<GenshinUserInventoryItem, String> {

    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    Optional<GenshinUserInventoryItem> findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

}
