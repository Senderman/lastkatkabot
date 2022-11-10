package com.senderman.lastkatkabot.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class Threads {

    // to make Threads.sleep throw uncheked exception
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A utility class to be used when creating ExecutorService
     * Makes possible to give a name format for all Executor's threads
     * %d in the threadNameFormat will be replaced with current thread counter
     */
    public static class NamedThreadFactory implements ThreadFactory {

        private final String threadNameFormat;
        private int threadsCreated;

        public NamedThreadFactory(String threadNameFormat) {
            this.threadsCreated = 0;
            this.threadNameFormat = threadNameFormat;
        }

        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            String threadName = String.format(threadNameFormat, threadsCreated++);
            return new Thread(runnable, threadName);
        }
    }
}
