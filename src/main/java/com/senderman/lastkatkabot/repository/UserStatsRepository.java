package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Userstats;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends CrudRepository<Userstats, Long> {

    Optional<Userstats> findByLoverId(int loverId);

    List<Userstats> findTop10ByOrderByBncScoreDesc();

    List<Userstats> findTop10ByOrderByBncScoreDescByIdIn(List<Long> Ids);

}
