package com.senderman.lastkatkabot.util;

public class Threads {

    // to make Threads.sleep throw unchecked exception
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
