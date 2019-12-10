package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService

class LastkatkaBot : BotModule {
    override fun botHandler(config: Config): BotHandler {
        val configLoader = YamlConfigLoaderService<BotConfig>()
        val configFile = configLoader.configFile("botConfigs/lastkatkabot", config.profile)
        val botConfig = configLoader.load(configFile, BotConfig::class.java)
        Services.botConfig = botConfig
        return LastkatkaBotHandler()
    }

    companion object {
        const val CALLBACK_REGISTER_IN_TOURNAMENT = "register_in_tournament"
        const val CALLBACK_PAY_RESPECTS = "pay_respects"
        const val CALLBACK_CAKE_OK = "cake_ok"
        const val CALLBACK_CAKE_NOT = "cake_not"
        const val CALLBACK_JOIN_DUEL = "join_duel"
        const val CALLBACK_CLOSE_MENU = "close_menu"
        const val CALLBACK_DELETE_ADMIN = "deleteuser_admin"
        const val CALLBACK_DELETE_NEKO = "deleteuser_neko"
        const val CALLBACK_DELETE_PREM = "deleteuser_prem"
        const val CALLBACK_VOTE_BNC = "vote_bnc"
        const val CALLBACK_ACCEPT_MARRIAGE = "acc_marr "
        const val CALLBACK_DENY_MARRIAGE = "deny_marr "
        const val CALLBACK_ANSWER_FEEDBACK = "answ_feedback "
        const val CALLBACK_BLOCK_USER = "block_user"
    }
}