package com.senderman.lastkatkabot.feature.access.service;

import com.senderman.lastkatkabot.feature.access.model.AdminUser;
import com.senderman.lastkatkabot.feature.access.repository.AdminUserRepository;
import jakarta.inject.Named;
import jakarta.inject.Singleton;


@Singleton
@Named("adminManager")
public class AdminService extends UserManager<AdminUser> {

    public AdminService(AdminUserRepository repository) {
        super(repository);
    }
}
