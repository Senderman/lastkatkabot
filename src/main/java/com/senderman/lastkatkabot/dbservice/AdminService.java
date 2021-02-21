package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;
import org.springframework.stereotype.Service;

@Service("adminManager")
public class AdminService extends UserManager<AdminUser> {

    public AdminService(AdminUserRepository repository) {
        super(repository);
    }
}
