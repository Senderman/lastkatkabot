package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Feedback;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {

    Optional<Feedback> findFirstByOrderByIdDesc();

    long deleteByIdBetween(int from, int to);

}
