package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Feedback;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {

    Optional<Feedback> findFirstByOrderByIdDesc();

}
