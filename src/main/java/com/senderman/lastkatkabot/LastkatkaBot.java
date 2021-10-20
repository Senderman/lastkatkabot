package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;

import java.util.List;

public class LastkatkaBot implements BotModule {

    public static void main(String[] args) {
        Runner.run("", List.of(new LastkatkaBot()));
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        var handlerClass = BotHandler.class;
        var context = SpringApplication.run(handlerClass);
        var serviceLauncher = context.getBean(BackgoundServiceLauncher.class);
        serviceLauncher.runServices();
        return context.getBean(handlerClass);
    }
}
