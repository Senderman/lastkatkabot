package com.senderman.lastkatkabot.feature.feedback.service;

import com.senderman.lastkatkabot.feature.feedback.model.Feedback;
import com.senderman.lastkatkabot.feature.feedback.repository.FeedbackRepository;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class PgsqlFeedbackService implements FeedbackService {

    private final FeedbackRepository repository;

    public PgsqlFeedbackService(FeedbackRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Feedback> findById(int id) {
        return repository.findById(id);
    }

    @Override
    public void deleteById(int id) {
        repository.deleteById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public boolean existsById(int id) {
        return repository.existsById(id);
    }

    @Override
    public Iterable<Feedback> findAll() {
        return repository.findAll();
    }

    @Override
    public Feedback insert(Feedback feedback) {
        return repository.save(feedback);
    }

    @Override
    public Feedback update(Feedback feedback) {
        return repository.update(feedback);
    }

    @Override
    public long deleteByIdBetween(int from, int to) {
        return repository.deleteByIdBetween(from, to);
    }
}
