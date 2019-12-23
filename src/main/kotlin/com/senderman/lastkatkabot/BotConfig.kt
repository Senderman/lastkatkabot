package com.senderman.lastkatkabot

import com.fasterxml.jackson.annotation.JsonProperty

class BotConfig {
    @JsonProperty(required = true)
    lateinit var login: String

    @JsonProperty
    var mainAdmin = 0

    @JsonProperty
    var lastvegan: Long = 0

    @JsonProperty
    var tourgroup: Long = 0

    @JsonProperty
    lateinit var tourchannel: String

    @JsonProperty
    lateinit var tourgroupname: String

    @JsonProperty
    lateinit var wwBots: Set<String>

    @JsonProperty
    lateinit var bncphoto: String

    @JsonProperty
    lateinit var leavesticker: String

    @JsonProperty
    lateinit var higif: String

    @JsonProperty
    lateinit var setupHelp: String

    @JsonProperty
    lateinit var loveStrings: Array<String>

}