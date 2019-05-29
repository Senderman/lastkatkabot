package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.MissingResourceException;

public class LastResourceBundleLocalizationService extends ResourceBundleLocalizationService {

    private final DBService db;

    public LastResourceBundleLocalizationService(String resourceName, DBService dbService) {
        super(resourceName);
        db = dbService;
    }

    public String getLocale(Message message) {
        if (message.isUserMessage())
            return db.getUserLocale(message.getFrom().getId());
        else
            return db.getChatLocale(message.getChatId());
    }

    public String getLocale(CallbackQuery query) {
        return db.getUserLocale(query.getFrom().getId());
    }

    @Override
    public String getString(String key, String language) {
        try {
            return super.getString(key, language);
        } catch (MissingResourceException e) {
            return super.getString(key, "en");
        }
    }

    public String getString(String key, User user) {
        return getString(key, db.getUserLocale(user.getId()));
    }

    public String getString(String key, Chat chat) {
        return getString(key, db.getChatLocale(chat.getId()));
    }

    public String getString(String key, Message message) {
        if (message.isUserMessage())
            return getString(key, message.getFrom());
        else
            return getString(key, message.getChat());
    }
}
