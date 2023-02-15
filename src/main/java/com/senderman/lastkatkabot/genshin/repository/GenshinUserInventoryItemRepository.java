package com.senderman.lastkatkabot.genshin.repository;

import com.senderman.lastkatkabot.genshin.model.GenshinUserInventoryItem;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@MongoRepository
public interface GenshinUserInventoryItemRepository extends CrudRepository<GenshinUserInventoryItem, String> {

    @MongoFindQuery(value = "{ chatId: ?0, userId: ?1 }", sort = "{ stars: -1 }")
    List<GenshinUserInventoryItem> findByChatIdAndUserId(long chatId, long userId);

    Optional<GenshinUserInventoryItem> findByChatIdAndUserIdAndItemId(long chatId, long userId, String itemId);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends GenshinUserInventoryItem> S update(@Valid @NotNull S entity);
}
