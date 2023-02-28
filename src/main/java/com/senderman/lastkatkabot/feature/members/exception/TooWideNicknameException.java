package com.senderman.lastkatkabot.feature.members.exception;

public class TooWideNicknameException extends Exception {
    public TooWideNicknameException(String nickname) {
        super("Nickname " + nickname + " is too wide!");
    }
}