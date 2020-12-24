package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

public class LastkatkaBot implements BotModule {

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        var context = SpringApplication.run(UpdateHandler.class);
        var botHandler = context.getBean(UpdateHandler.class);
        return botHandler;
    }

    public static void main(String[] args) {
        Runner.run("", List.of(new LastkatkaBot()));
    }
}
