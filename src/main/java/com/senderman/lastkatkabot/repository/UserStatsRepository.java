package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.Userstats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends MongoRepository<Userstats, Integer> {

    Userstats findByUserId(int userId);
    Userstats findByLoverId(int loverId);

}
