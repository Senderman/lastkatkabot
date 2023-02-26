package com.senderman.lastkatkabot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface UpdateOffloader {

    CompletableFuture<Void> offloadUpdates(Collection<Update> updates);

}
