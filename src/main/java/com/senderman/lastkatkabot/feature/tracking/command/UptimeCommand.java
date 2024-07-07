package com.senderman.lastkatkabot.feature.tracking.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;

@Command
public class UptimeCommand implements CommandExecutor {

    @Override
    public String command() {
        return "/uptime";
    }

    @Override
    public String getDescriptionKey() {
        return "tracking.uptime.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        ctx.replyToMessage(formatHealth(ctx)).callAsync(ctx.sender);
    }

    private String formatHealth(L10nMessageContext ctx) {
        var r = Runtime.getRuntime();
        double delimiter = 1048576f;
        return ctx.getString("tracking.uptime.text")
                .formatted(
                        (r.totalMemory() - r.freeMemory()) / delimiter,
                        r.freeMemory() / delimiter,
                        r.totalMemory() / delimiter,
                        r.maxMemory() / delimiter,
                        getNativeMemory() / delimiter,
                        formatTime(ManagementFactory.getRuntimeMXBean().getUptime(), ctx),
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

    private String formatTime(long millis, L10nMessageContext ctx) {
        long secs = millis / 1000;

        long mins = secs / 60;
        secs -= mins * 60;

        long hours = mins / 60;
        mins -= hours * 60;

        long days = hours / 24;
        hours -= days * 24;

        return ctx.getString("tracking.uptime.time").formatted(days, hours, mins, secs);
    }
}
