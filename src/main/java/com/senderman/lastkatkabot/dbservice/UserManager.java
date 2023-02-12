package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.IdAndName;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class UserManager<TUserEntity extends IdAndName<Long>> {

    private final CrudRepository<TUserEntity, Long> repository;
    private final Set<Long> userIds;

    public UserManager(CrudRepository<TUserEntity, Long> repository) {
        this.repository = repository;
        this.userIds = StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(IdAndName::getId)
                .collect(Collectors.toSet());
    }

    public Iterable<TUserEntity> findAll() {
        return repository.findAll();
    }

    public boolean hasUser(long id) {
        return userIds.contains(id);
    }

    public boolean addUser(TUserEntity entity) {
        if (userIds.contains(entity.getId())) return false;
        userIds.add(entity.getId());
        repository.save(entity);
        return true;
    }

    public boolean deleteById(long id) {
        if (!userIds.remove(id)) return false;
        repository.deleteById(id);
        return true;
    }


}
