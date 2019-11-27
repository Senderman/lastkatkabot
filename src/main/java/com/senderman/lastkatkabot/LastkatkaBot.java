package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.beans.Config;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import org.jetbrains.annotations.NotNull;

public class LastkatkaBot implements BotModule {

    public static final String CALLBACK_REGISTER_IN_TOURNAMENT = "register_in_tournament";
    public static final String CALLBACK_PAY_RESPECTS = "pay_respects";
    public static final String CALLBACK_CAKE_OK = "cake ok";
    public static final String CALLBACK_CAKE_NOT = "cake not";
    public static final String CALLBACK_JOIN_DUEL = "join_duel";
    public static final String CALLBACK_ALLOW_CHAT = "allow_chat";
    public static final String CALLBACK_DONT_ALLOW_CHAT = "dont_allow_chat";
    public static final String CALLBACK_DELETE_CHAT = "delete_chat";
    public static final String CALLBACK_CLOSE_MENU = "close_menu";
    public static final String CALLBACK_DELETE_ADMIN = "deleteuser_admin";
    public static final String CALLBACK_DELETE_NEKO = "deleteuser_neko";
    public static final String CALLBACK_DELETE_PREM = "deleteuser_prem";
    public static final String CALLBACK_VOTE_BNC = "vote_bnc";
    public static final String CALLBACK_ACCEPT_MARRIAGE = "accept_marriage";
    public static final String CALLBACK_DENY_MARRIAGE = "deny_marriage";

    @NotNull
    @Override
    public BotHandler botHandler(@NotNull Config config) {
        final var configLoader = new YamlConfigLoaderService<BotConfig>();
        final var configFile = configLoader.configFile("botConfigs/lastkatkabot", config.getProfile());
        final var botConfig = configLoader.load(configFile, BotConfig.class);
        Services.setBotConfig(botConfig);
        return new LastkatkaBotHandler();
    }
}