package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import org.springframework.stereotype.Component;

@Component("adminManager")
public class AdminManager extends UserManager<AdminUser> {

    public AdminManager(AdminUserRepository repository) {
        super(repository);
    }
}
