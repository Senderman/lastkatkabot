package com.senderman.anitrackerbot

interface DBService {
    fun saveAnime(id: Int, userId: Int, url: String)
    fun getAnimeUrl(id: Int, userId: Int): String
    fun deleteAnime(id: Int, userId: Int)
    fun dropUser(userId: Int)
    fun totalAnimes(userId: Int): Int
    fun idExists(id: Int, userId: Int): Boolean
    fun urlExists(url: String, userId: Int): Boolean
    fun getAllAnimes(userId: Int): Map<Int, String>
    fun getUsersIds(): List<Int>
}