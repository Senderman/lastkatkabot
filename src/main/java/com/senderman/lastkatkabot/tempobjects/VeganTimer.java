package com.senderman.lastkatkabot.tempobjects;

import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.HashSet;
import java.util.Set;

public class VeganTimer {
    private final long chatId;
    private final Set<Integer> vegans;
    private boolean runTimer = true;

    public VeganTimer(long chatId) {
        this.chatId = chatId;
        vegans = new HashSet<>();
        new Thread(this::startVeganTimer).start();
    }

    private void startVeganTimer() {
        try {
            Thread.sleep(60000);
            for (int i = 4; i > 0 && runTimer; i--) {
                Services.handler().sendMessage(chatId,
                        String.format("Осталось %1$d минуты чтобы джойнуться\n\n" +
                                "Джоин --> /join@veganwarsbot", i));
                Thread.sleep(60000);

            }
        } catch (InterruptedException e) {
            stop();
            BotLogger.error("THREAD SLEEP", e.toString());
            Services.handler().sendMessage(chatId, "Ошибка, таймер остановлен");
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
        String toSend = String.format("Джойнулось %1$d игроков", count);
        if (count % 2 != 0 && count > 2) {
            toSend += "\nБудет крыса!";
        }
        Services.handler().sendMessage(chatId, toSend);
    }

    public void removePlayer(int id) {
        if (!vegans.contains(id))
            return;

        vegans.remove(id);
        int count = getVegansAmount();
        String toSend = String.format("Осталось %1$d игроков", count);
        if (count % 2 != 0 && count > 2) {
            toSend += "\nБудет крыса!";
        }
        Services.handler().sendMessage(chatId, toSend);
    }

    public int getVegansAmount() {
        return vegans.size();
    }
}