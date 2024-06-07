package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface BncRepository extends CrudRepository<BncGameSave, Long> {

    void deleteByEditDateLessThan(Timestamp editDate);

}
