package com.senderman.anitrackerbot

import com.fasterxml.jackson.annotation.JsonProperty

class BotConfig {
    @JsonProperty
    var token: String? = null

    @JsonProperty
    var username: String? = null

    @JsonProperty
    var help: String? = null

    @JsonProperty
    var anidata: String? = null

    @JsonProperty
    var position = 0

}