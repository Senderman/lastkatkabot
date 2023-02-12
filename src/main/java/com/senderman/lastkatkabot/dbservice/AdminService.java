package com.senderman.lastkatkabot.dbservice;

import com.senderman.lastkatkabot.model.AdminUser;
import com.senderman.lastkatkabot.repository.AdminUserRepository;


@Service("adminManager")
public class AdminService extends UserManager<AdminUser> {

    public AdminService(AdminUserRepository repository) {
        super(repository);
    }
}
