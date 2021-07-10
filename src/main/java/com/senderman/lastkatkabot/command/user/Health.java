package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.service.CachingUserActivityTrackerService;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

@Component
public class Health implements CommandExecutor {

    private final CachingUserActivityTrackerService trackerService;

    public Health(CachingUserActivityTrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public String command() {
        return "/health";
    }

    @Override
    public String getDescription() {
        return "здоровье бота";
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.reply(formatHealth()).callAsync(ctx.sender);
    }

    private String formatHealth() {
        var r = Runtime.getRuntime();
        double delimiter = 1048576f;
        return String.format(
                "<b>Нагрузка:</b>\n\n" +
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
                CachingUserActivityTrackerService.FLUSH_INTERVAL
        );
    }
}
