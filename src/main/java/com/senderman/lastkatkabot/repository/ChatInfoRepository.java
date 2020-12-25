package com.senderman.lastkatkabot.repository;

import com.senderman.lastkatkabot.model.ChatInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatInfoRepository extends MongoRepository<ChatInfo, Long> {

}
