package com.senderman.anitrackerbot

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.senderman.MongoClientKeeper
import org.bson.Document
import java.util.*
import kotlin.collections.ArrayList

internal class MongoDBService : DBService {
    private val client = MongoClientKeeper.client
    private val database = client.getDatabase("anime")

    private fun user(id: Int): MongoCollection<Document> = database.getCollection(id.toString())

    override fun saveAnime(id: Int, userId: Int, url: String) {
        val doc = Document("id", id).append("url", url)
        user(userId).insertOne(doc)
    }

    override fun getAnimeUrl(id: Int, userId: Int): String {
        return user(userId).find(eq("id", id)).first()!!.getString("url")
    }

    override fun deleteAnime(id: Int, userId: Int) {
        user(userId).deleteOne(eq("id", id))
    }

    override fun dropUser(userId: Int) {
        user(userId).drop()
    }

    override fun totalAnimes(userId: Int): Int {
        return user(userId).countDocuments().toInt()
    }

    override fun idExists(id: Int, userId: Int): Boolean {
        return user(userId).find(eq("id", id)).first() != null
    }

    override fun urlExists(url: String, userId: Int): Boolean {
        return user(userId).find(eq("url", url)).first() != null
    }

    override fun getAllAnimes(userId: Int): Map<Int, String> {
        val user = user(userId).find()
        val result: MutableMap<Int, String> = HashMap()
        for (doc in user) {
            result[doc.getInteger("id")] = doc.getString("url")
        }
        return result
    }

    override fun getUsersIds(): List<Int> {
        val result = ArrayList<Int>()
        for (userId in database.listCollectionNames()) {
            result.add(userId.toInt())
        }
        return result;
    }
}