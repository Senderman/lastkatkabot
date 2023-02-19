package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.BlacklistedUser;
import com.senderman.lastkatkabot.repository.BlacklistedUserRepository;
import jakarta.inject.Named;
import jakarta.inject.Singleton;


@Singleton
@Named("blacklistManager")
public class BlacklistService extends UserManager<BlacklistedUser> {

    public BlacklistService(BlacklistedUserRepository repository) {
        super(repository);
    }
}
