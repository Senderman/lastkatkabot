package com.senderman

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients

object MongoClientKeeper {
    val client: MongoClient = MongoClients.create(System.getenv("database"))
}