package com.senderman.lastkatkabot.feature.tracking.command;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;

@Command
public class UptimeCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/uptime";
    }

    @Override
    public String getDescription() {
        return "посмотреть нагрузку на бота";
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.replyToMessage(formatHealth()).callAsync(ctx.sender);
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
                Нативная память: <code>%.2f MiB</code>
                Аптайм: <code>%s</code>
                Потоки: <code>%d</code>
                CPUs: <code>%d</code>"""
                .formatted(
                        (r.totalMemory() - r.freeMemory()) / delimiter,
                        r.freeMemory() / delimiter,
                        r.totalMemory() / delimiter,
                        r.maxMemory() / delimiter,
                        getNativeMemory() / delimiter,
                        formatTime(ManagementFactory.getRuntimeMXBean().getUptime()),
                        ManagementFactory.getThreadMXBean().getThreadCount(),
                        ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors()
                );
    }

    private long getNativeMemory() {
        return ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .filter(b -> b.getType().equals(MemoryType.NON_HEAP))
                .mapToLong(b -> b.getUsage().getUsed())
                .sum();
    }

    private String formatTime(long millis) {
        long secs = millis / 1000;

        long mins = secs / 60;
        secs -= mins * 60;

        long hours = mins / 60;
        mins -= hours * 60;

        long days = hours / 24;
        hours -= days * 24;

        return "%dдн, %dч, %dмин, %dсек".formatted(days, hours, mins, secs);
    }
}
