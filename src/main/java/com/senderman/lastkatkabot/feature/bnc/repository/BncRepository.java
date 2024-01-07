package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncGameSave;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface BncRepository extends CrudRepository<BncGameSave, Long> {

    List<BncGameSave> findByEditDateLessThan(Timestamp editDate);

}
