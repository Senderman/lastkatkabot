package com.senderman.lastkatkabot.feature.genshin.repository;

import com.senderman.lastkatkabot.feature.genshin.model.GenshinUserInventoryItem;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@MongoRepository
public interface GenshinUserInventoryItemRepository extends CrudRepository<GenshinUserInventoryItem, String> {

    @MongoFindQuery(value = "{ chatId: :chatId, userId: :userId }", sort = "{ stars: -1 }")
    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    Optional<GenshinUserInventoryItem> findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends GenshinUserInventoryItem> S update(@NonNull S entity);
}
