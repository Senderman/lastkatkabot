package com.senderman.lastkatkabot.feature.bnc.repository;

import com.senderman.lastkatkabot.feature.bnc.model.BncRecord;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface BncRecordRepository extends CrudRepository<BncRecord, BncRecord.PrimaryKey> {

    List<BncRecord> findAllOrderByLengthAndHexadecimal();

}
