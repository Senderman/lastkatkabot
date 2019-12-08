package com.senderman

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import java.util.logging.Level
import java.util.logging.Logger

object MongoClientKeeper {
    val client: MongoClient = makeClient()

    private fun makeClient(): MongoClient {
        val logger = Logger.getLogger("org.mongodb.driver")
        logger.level = Level.SEVERE
        return MongoClients.create(System.getenv("database"))
    }
}