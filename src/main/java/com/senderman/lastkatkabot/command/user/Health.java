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

    private final CachingUserActivityTrackerService trackerService;

    public Health(CachingUserActivityTrackerService trackerService) {
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
    public void execute(Message message, CommonAbsSender telegram) {
        Methods.sendMessage(message.getChatId(), formatHealth()).callAsync(telegram);
    }

    private String formatHealth() {
        var r = Runtime.getRuntime();
        double delimiter = 1048576f;
        return """
                🖥 <b>Нагрузка:</b>

                Занято: <code>%.2f MiB</code>
                Свободно: <code>%.2f MiB</code>
                Выделено JVM: <code>%.2f MiB</code>
                Доступно JVM: <code>%.2f MiB</code>
                Аптайм: <code>%d min</code>
                Потоки: <code>%d</code>
                CPUs: <code>%d</code>
                Средний сброс кеша трекера юзеров: %d/%ds"""
                .formatted(
                        (r.totalMemory() - r.freeMemory()) / delimiter,
                        r.freeMemory() / delimiter,
                        r.totalMemory() / delimiter,
                        r.maxMemory() / delimiter,
                        ManagementFactory.getRuntimeMXBean().getUptime() / 60000,
                        ManagementFactory.getThreadMXBean().getThreadCount(),
                        ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors(),
                        trackerService.getAvgCacheFlushingSize(),
                        CachingUserActivityTrackerService.FLUSH_INTERVAL
                );
    }
}
