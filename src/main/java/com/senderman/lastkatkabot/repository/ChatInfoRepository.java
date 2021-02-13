package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface ChatInfoRepository extends CrudRepository<ChatInfo, Long> {

    long deleteByChatIdIn(Collection<Long> chatIds);

}
