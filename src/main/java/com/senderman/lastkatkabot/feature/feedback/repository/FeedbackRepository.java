package com.senderman.lastkatkabot.feature.feedback.repository;

import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {

    long deleteByIdBetween(int from, int to);

}
