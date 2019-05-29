package com.senderman.futurewars;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.beans.Config;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import org.jetbrains.annotations.NotNull;

public class FutureWarsBot implements BotModule {

    static final String CALLBACK_ATTACK = "attack "; // set up laser after selecting target
    static final String CALLBACK_DEFENCE = "defence "; // set up shield
    static final String CALLBACK_FLAG_DEFENCE = "flag_defence ";
    static final String CALLBACK_CHARGE_LASER = "charge_laser ";
    static final String CALLBACK_CHARGE_SHIELD = "charge_shield ";
    static final String CALLBACK_CONFIRM_DEFENCE = "confirm_defence ";
    static final String CALLBACK_CONFIRM_FLAG_DEFENCE = "confirm_flag_defence ";
    static final String CALLBACK_DEFENCE_VALUE = "defence_value "; // change value of shield
    static final String CALLBACK_SELECT_TARGET = "select_target "; // show list of enemies/teammates
    static final String CALLBACK_MAIN_MENU = "main_menu ";
    static final String CALLBACK_JOIN_TEAM = "join_team ";
    static final String CALLBACK_SUMMON_CLONE = "summon_clone ";
    static final String CALLBACK_ROLL = "roll ";

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        final var configLoader = new YamlConfigLoaderService<BotConfig>();
        final var configFile = configLoader.configFile("botConfigs/futurewarsconfig", config.getProfile());
        final var botConfig = configLoader.load(configFile, BotConfig.class);
        return new FutureWarsHandler(botConfig);
    }
}
