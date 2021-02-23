package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.service.CachingUserActivityTrackerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.lang.management.ManagementFactory;

@Component
public class Health implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final CachingUserActivityTrackerService trackerService;

    public Health(CommonAbsSender telegram, CachingUserActivityTrackerService trackerService) {
        this.telegram = telegram;
        this.trackerService = trackerService;
    }

    @Override
    public String getTrigger() {
        return "/health";
    }

    @Override
    public String getDescription() {
        return "здоровье бота";
    }

    @Override
    public void execute(Message message) {
        Methods.sendMessage(message.getChatId(), formatHealth()).callAsync(telegram);
    }

    private String formatHealth() {
        var r = Runtime.getRuntime();
        double delimiter = 1048576f;
        return String.format("\uD83D\uDDA5 <b>Нагрузка:</b>\n\n" +
                        "Занято: <code>%.2f MiB</code>\n" +
                        "Свободно: <code>%.2f MiB</code>\n" +
                        "Выделено JVM: <code>%.2f MiB</code>\n" +
                        "Доступно JVM: <code>%.2f MiB</code>\n" +
                        "Аптайм: <code>%d min</code>\n" +
                        "Потоки: <code>%d</code>\n" +
                        "CPUs: <code>%d</code>\n" +
                        "Средний сброс кеша трекера юзеров: %d/%ds",
                (r.totalMemory() - r.freeMemory()) / delimiter,
                r.freeMemory() / delimiter,
                r.totalMemory() / delimiter,
                r.maxMemory() / delimiter,
                ManagementFactory.getRuntimeMXBean().getUptime() / 60000,
                ManagementFactory.getThreadMXBean().getThreadCount(),
                ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors(),
                trackerService.getAvgCacheFlushingSize(),
                CachingUserActivityTrackerService.FLUSH_INTERVAL);
    }
}
