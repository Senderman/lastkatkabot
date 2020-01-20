package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService
import org.apache.commons.lang3.StringEscapeUtils

class LastkatkaBot : BotModule {
    override fun botHandler(config: Config): BotHandler {
        val configLoader = YamlConfigLoaderService<BotConfig>()
        val configFile = configLoader.configFile("botConfigs/lastkatkabot", config.profile)
        val botConfig = configLoader.load(configFile, BotConfig::class.java)
        Services.botConfig = botConfig
        return LastkatkaBotHandler()
    }

    companion object {
        fun formatJSON(json: String): String {
            val replacements = mapOf(
                "[ ,]*\\w+='?null'?" to "",
                "(\\w*[iI]d=)(-?\\d+)" to "$1<code>$2</code>",
                "([{,])" to "$1\n",
                "(})" to "\n$1",
                "(=)" to " $1 "
            )
            var result = json
            for ((old, new) in replacements) {
                result = result.replace(old.toRegex(), new)
            }
            return StringEscapeUtils.unescapeJava(result)
        }
    }
}

fun main(args: Array<String>) {
    val profile = if (args.isNotEmpty() && args[0].isNotEmpty()) args[0] else ""
    Runner.run(profile, listOf(LastkatkaBot()))
}