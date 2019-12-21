package com.senderman.anitrackerbot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService

class AnitrackerBot : BotModule {
    override fun botHandler(config: Config): BotHandler {
        val configLoader = YamlConfigLoaderService<BotConfig>()
        val configfile = configLoader.configFile("botConfigs/anime", config.profile)
        val botConfig = configLoader.load(configfile, BotConfig::class.java)
        return AnitrackerBotHandler(botConfig)
    }
}