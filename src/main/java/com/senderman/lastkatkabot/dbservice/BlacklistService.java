package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;


@Service("blacklistManager")
public class BlacklistService extends UserManager<BlacklistedUser> {

    public BlacklistService(BlacklistedUserRepository repository) {
        super(repository);
    }
}
