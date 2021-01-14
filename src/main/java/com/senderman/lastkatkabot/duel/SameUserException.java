package com.senderman.lastkatkabot.duel;

public class SameUserException extends RuntimeException {

    public SameUserException(int userId) {
        super("UserId " + userId + " already exists");
    }

}
