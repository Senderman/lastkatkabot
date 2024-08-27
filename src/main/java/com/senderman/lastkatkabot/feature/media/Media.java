package com.senderman.lastkatkabot.feature.media;

import java.util.regex.Pattern;

public enum Media {

    BNCHELP("media.bnchelp", "/media/bnchelp.png"),
    GREETING_GIF("media.greeting-gif", "/media/greeting_gif.mp4"),
    LEAVE_STICKER("media.leave-sticker", "/media/leave_sticker.webp");

    private final static Pattern leaveName = Pattern.compile(".*/");
    private final String key;
    private final String path;

    Media(String key, String path) {
        this.key = key;
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return leaveName.matcher(path).replaceAll("");
    }
}
