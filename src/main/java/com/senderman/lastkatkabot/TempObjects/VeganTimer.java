package com.senderman.lastkatkabot.TempObjects;

import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.HashSet;
import java.util.Set;

public class VeganTimer {
    private final long chatId;
    private final String locale;
    private Set<Integer> vegans;
    private boolean runTimer = true;

    public VeganTimer(long chatId) {
        this.chatId = chatId;
        locale = Services.db().getChatLocale(chatId);
        vegans = new HashSet<>();
        new Thread(this::startVeganTimer).start();
    }

    private void startVeganTimer() {
        for (int i = 299; i > 0 && runTimer; i--) {

            if (i % 60 == 0) {
                Services.handler().sendMessage(chatId,
                        String.format(Services.i18n().getString("joinTime", locale), i / 60));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                stop();
                BotLogger.error("THREAD SLEEP", e.toString());
                Services.handler().sendMessage(chatId, "Ошибка, таймер остановлен");
            }
        }
        stop();
    }

    public void stop() {
        runTimer = false;
        Services.handler().veganTimers.remove(chatId);
    }

    public void addPlayer(int id, Message message) {
        if (vegans.contains(message.getFrom().getId()))
            return;

        vegans.add(id);
        int count = vegans.size();
        String toSend = String.format(Services.i18n().getString("playersJoined", locale), count);
        if (count % 2 != 0 && count > 2) {
            toSend += "\n" + Services.i18n().getString("ratWarning", locale);
        }
        Services.handler().sendMessage(chatId, toSend);
    }

    public void removePlayer(int id) {
        if (!vegans.contains(id))
            return;

        vegans.remove(id);
        int count = getVegansAmount();
        String toSend = String.format(Services.i18n().getString("playersLeft", locale), count);
        if (count % 2 != 0 && count > 2) {
            toSend += "\n" + Services.i18n().getString("ratWarning", locale);
        }
        Services.handler().sendMessage(chatId, toSend);
    }

    public int getVegansAmount() {
        return vegans.size();
    }
}