package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BncGameSave;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface BncRepository extends CrudRepository<BncGameSave, Long> {

    List<BncGameSave> deleteByEditDateLessThan(int editDate);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends BncGameSave> S update(@Valid @NotNull S entity);
}
