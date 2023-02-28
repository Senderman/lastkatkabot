package com.senderman.lastkatkabot.feature.access.service;

import com.senderman.lastkatkabot.feature.access.model.BlacklistedUser;
import com.senderman.lastkatkabot.feature.access.repository.BlacklistedUserRepository;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("blacklistManager")
public class BlacklistService extends UserManager<BlacklistedUser> {

    public BlacklistService(BlacklistedUserRepository repository) {
        super(repository);
    }
}
