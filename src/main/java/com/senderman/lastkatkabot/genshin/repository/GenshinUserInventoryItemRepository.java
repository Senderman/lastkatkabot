package com.senderman.lastkatkabot.genshin.repository;

import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenshinUserInventoryItemRepository extends CrudRepository<GenshinUserInventoryItem, String> {

    @Query(value = "{ chatId: ?0, userId: ?1 }", sort = "{ stars: -1 }")
    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    Optional<GenshinUserInventoryItem> findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

}
