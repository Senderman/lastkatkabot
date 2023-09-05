package com.senderman.lastkatkabot.feature.feedback.service;

import com.senderman.lastkatkabot.feature.feedback.model.Feedback;

import java.util.Optional;

public interface FeedbackService {

    Optional<Feedback> findById(int id);

    void deleteById(int id);

    long count();

    boolean existsById(int id);

    Iterable<Feedback> findAll();

    /**
     * Insert new feedback into database. Note that the id can be changed, so use the returned object
     *
     * @param feedback feedback to insert
     * @return the actually saved object (id can be different)
     */
    Feedback insert(Feedback feedback);

    Feedback update(Feedback feedback);

    /**
     * Delete feedbacks in range
     *
     * @param from lower bound (inclusive)
     * @param to   upper bound (inclusive)
     * @return amount of deleted feedbacks
     */
    long deleteByIdBetween(int from, int to);

}
