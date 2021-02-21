package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import org.springframework.stereotype.Service;

@Service("blacklistManager")
public class BlacklistService extends UserManager<BlacklistedUser> {

    public BlacklistService(BlacklistedUserRepository repository) {
        super(repository);
    }
}
