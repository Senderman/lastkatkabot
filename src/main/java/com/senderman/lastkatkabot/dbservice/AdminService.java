package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import jakarta.inject.Named;
import jakarta.inject.Singleton;


@Singleton
@Named("adminManager")
public class AdminService extends UserManager<AdminUser> {

    public AdminService(AdminUserRepository repository) {
        super(repository);
    }
}
