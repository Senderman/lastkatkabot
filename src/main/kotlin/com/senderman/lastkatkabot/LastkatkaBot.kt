package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService
import org.apache.commons.lang3.StringEscapeUtils
import java.io.File
import java.io.FileOutputStream

class LastkatkaBot : BotModule {

    private val configDir = File("botConfigs")
    private val configFile = File("${configDir.name}/lastkatkabot.yaml")

    private fun unpackConfig() {
        if (!configDir.exists())
            configDir.mkdir()
        if (!configDir.isDirectory)
            throw Exception(
                "${configDir.name} is not a directory! Please, remove the \"${configDir.name}\" file from project's root!"
            )

        if (configFile.exists())
            return
        val configFileRes = this::class.java.getResourceAsStream("/lastkatkabot.yaml")
        val buffer = ByteArray(configFileRes.available())
        configFileRes.read(buffer)
        val outStream = FileOutputStream(configFile)
        outStream.write(buffer)
        outStream.flush()
        outStream.close()
    }

    override fun botHandler(config: Config): BotHandler {
        unpackConfig()
        val configLoader = YamlConfigLoaderService<BotConfig>()
        val botConfig = configLoader.load(configFile, BotConfig::class.java)
        configFile.delete()
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
