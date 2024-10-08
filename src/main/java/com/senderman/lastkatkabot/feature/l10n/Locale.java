package com.senderman.lastkatkabot.feature.l10n;

public enum Locale {
    ENGLISH("\uD83C\uDDEC\uD83C\uDDE7 English", "en"),
    RUSSIAN("\uD83C\uDDF7\uD83C\uDDFA Русский", "ru"),
    UKRAINIAN("\uD83C\uDDFA\uD83C\uDDE6 Украïнська", "uk");

    private final String name;
    private final String code;


    Locale(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
