package com.senderman.lastkatkabot.feature.genshin.repository;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinUserInventoryItem;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface GenshinUserInventoryItemRepository extends CrudRepository<GenshinUserInventoryItem, GenshinUserInventoryItem.PrimaryKey> {

    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    Optional<GenshinUserInventoryItem> findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

    @Query("""
            DELETE FROM genshin_user_inventory_item
            WHERE
            user_id NOT IN (SELECT DISTINCT user_id FROM genshin_chat_user)
            AND
            chat_id NOT IN (SELECT DISTINCT chat_id FROM genshin_chat_user);
            """)
    void deleteInactiveInventories();

}
