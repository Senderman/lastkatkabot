package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import org.springframework.stereotype.Component;

@Component("blacklistManager")
public class BlacklistManager extends UserManager<BlacklistedUser> {

    public BlacklistManager(BlacklistedUserRepository repository) {
        super(repository);
    }
}
