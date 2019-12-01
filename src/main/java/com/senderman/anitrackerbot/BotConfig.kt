package com.senderman.anitrackerbot

import com.fasterxml.jackson.annotation.JsonProperty

class BotConfig {
    @JsonProperty
    lateinit var token: String

    @JsonProperty
    lateinit var username: String

    @JsonProperty
    lateinit var help: String

    @JsonProperty
    lateinit var anidata: String

    @JsonProperty
    var position = 0

}