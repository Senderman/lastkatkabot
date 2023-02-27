package com.senderman.lastkatkabot.util;

import org.telegram.telegrambots.meta.api.objects.User;

public class Html {

    public static String getUserLink(User user) {
        return getUserLink(user.getId(), user.getFirstName());
    }

    public static String getUserLink(Long id, String name) {
        return "<a href=\"tg://user?id=" + id + "\">" + htmlSafe(name) + "</a>";
    }

    public static String htmlSafe(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
