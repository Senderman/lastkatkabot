package com.senderman.lastkatkabot.tempobjects

import com.senderman.TgUser

class BnCPlayer(id: Int, name: String, val score: Int) : Comparable<BnCPlayer>, TgUser(id, name) {
    override fun compareTo(other: BnCPlayer): Int = this.score - other.score
}