package com.senderman.lastkatkabot

import com.fasterxml.jackson.annotation.JsonProperty

class BotConfig {
    @JsonProperty(required = true)
    var token: String? = null

    @JsonProperty(required = true)
    var username: String? = null

    @JsonProperty
    var position = 0

    @JsonProperty
    var mainAdmin = 0

    @JsonProperty
    var lastvegan: Long = 0

    @JsonProperty
    var tourgroup: Long = 0

    @JsonProperty
    var tourchannel: String? = null

    @JsonProperty
    var tourgroupname: String? = null

    @JsonProperty
    var wwBots: Set<String>? = null

    @JsonProperty
    var bncphoto: String? = null

    @JsonProperty
    var leavesticker: String? = null

    @JsonProperty
    var higif: String? = null

    @JsonProperty
    var setupHelp: String? = null

    @JsonProperty
    var loveStrings: Array<String>? = null

}