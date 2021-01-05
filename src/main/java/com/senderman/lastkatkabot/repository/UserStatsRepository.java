package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Userstats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends CrudRepository<Userstats, Integer> {

    Optional<Userstats> findByLoverId(int loverId);

}
