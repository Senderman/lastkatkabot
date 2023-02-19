package com.senderman.lastkatkabot.util;

import org.telegram.telegrambots.meta.api.objects.User;

public class Html {

    public static String getUserLink(User user) {
        return "<a href=\"tg://user?id=" + user.getId() + "\">" + htmlSafe(user.getFirstName()) + "</a>";
    }

    public static String htmlSafe(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
