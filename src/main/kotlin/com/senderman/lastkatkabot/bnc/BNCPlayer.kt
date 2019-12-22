package com.senderman.lastkatkabot.bnc

import com.senderman.neblib.TgUser

class BNCPlayer(id: Int, name: String, val score: Int) : Comparable<BNCPlayer>, TgUser(id, name) {
    override fun compareTo(other: BNCPlayer) = this.score.compareTo(other.score)
}