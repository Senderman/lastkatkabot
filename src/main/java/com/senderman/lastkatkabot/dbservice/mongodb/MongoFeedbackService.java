package com.senderman.lastkatkabot.dbservice.mongodb;

import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.repository.FeedbackRepository;

import java.util.Optional;

@Singleton
public class MongoFeedbackService implements FeedbackService {

    private final FeedbackRepository repository;

    public MongoFeedbackService(FeedbackRepository repository) {
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
        // id as counter
        int id = repository.findFirstByOrderByIdDesc().map(f -> f.getId() + 1).orElse(1);
        feedback.setId(id);
        return repository.save(feedback);
    }

    @Override
    public Feedback update(Feedback feedback) {
        return repository.save(feedback);
    }

    @Override
    public long deleteByIdBetween(int from, int to) {
        return repository.deleteByIdBetween(from - 1, to + 1);
    }
}
