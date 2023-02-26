package com.senderman.lastkatkabot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;

public interface UpdateOffloader {

    void offloadUpdates(Collection<Update> updates);

}
