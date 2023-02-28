package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.annotation.MongoUpdateOptions;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@MongoRepository
public interface BncRepository extends CrudRepository<BncGameSave, Long> {

    @MongoFindQuery("{ editDate: { $lt: :editDate } }")
    List<BncGameSave> findByEditDateLessThan(int editDate);

    @Override
    @MongoUpdateOptions(upsert = true)
    <S extends BncGameSave> S update(@Valid @NotNull S entity);
}
