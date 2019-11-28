package com.senderman

import com.mongodb.client.MongoClients

object MongoClientKeeper {
    @JvmStatic
    val client = MongoClients.create(System.getenv("database"))
}