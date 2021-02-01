package com.senderman.lastkatkabot.command.user;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ShowMemory implements CommandExecutor {

    private final ApiRequests telegram;

    public ShowMemory(ApiRequests telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/memory";
    }

    @Override
    public String getDescription() {
        return "использование памяти";
    }

    @Override
    public void execute(Message message) {
        telegram.sendMessage(message.getChatId(), formatMemory());
    }

    private String formatMemory() {
        var r = Runtime.getRuntime();
        double delimiter = 1048576f;
        return String.format("\uD83D\uDDA5 <b>Память:</b>\n\n" +
                        "Занято: %.2f MiB\n" +
                        "Свободно: %.2f MiB\n" +
                        "Выделено JVM: %.2f MiB\n" +
                        "Доступно JVM: %.2f MiB",
                (r.totalMemory() - r.freeMemory()) / delimiter,
                r.freeMemory() / delimiter,
                r.totalMemory() / delimiter,
                r.maxMemory() / delimiter);
    }
}
