package com.senderman.lastkatkabot.feature.chatsettings.annotation;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Singleton
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandAccessCommand {
}
