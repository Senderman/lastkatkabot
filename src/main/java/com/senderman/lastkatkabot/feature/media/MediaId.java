package com.senderman.lastkatkabot.feature.media;

public enum MediaId {

    BNCHELP("media.bnchelp"),
    GREETING_GIF("media.greeting-gif"),
    LEAVE_STICKER("media.leave-sticker");

    private final String key;

    MediaId(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
