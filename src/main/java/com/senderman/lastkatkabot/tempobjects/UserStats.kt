package com.senderman.lastkatkabot.tempobjects

data class UserStats(
    val id: Int,
    val duelWins: Int,
    val totalDuels: Int,
    val bnc: Int,
    val loverId: Int = 0
)