package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.BncGameSave;

import java.util.List;

@Repository
public interface BncRepository extends CrudRepository<BncGameSave, Long> {

    List<BncGameSave> deleteByEditDateLessThan(int editDate);

}
