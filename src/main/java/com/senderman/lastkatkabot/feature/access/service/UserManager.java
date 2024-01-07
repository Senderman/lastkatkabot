package com.senderman.lastkatkabot.feature.access.service;

import com.senderman.lastkatkabot.feature.access.model.UserIdAndName;
import io.micronaut.data.repository.CrudRepository;

public abstract class UserManager<TUserEntity extends UserIdAndName<Long>> {

    private final CrudRepository<TUserEntity, Long> repository;

    public UserManager(CrudRepository<TUserEntity, Long> repository) {
        this.repository = repository;
    }

    public Iterable<TUserEntity> findAll() {
        return repository.findAll();
    }

    public boolean hasUser(long id) {
        return repository.existsById(id);
    }

    public boolean addUser(TUserEntity entity) {
        if (repository.existsById(entity.getUserId()))
            return false;
        repository.save(entity);
        return true;
    }

    public boolean deleteById(long id) {
        if (!repository.existsById(id))
            return false;
        repository.deleteById(id);
        return true;
    }


}
