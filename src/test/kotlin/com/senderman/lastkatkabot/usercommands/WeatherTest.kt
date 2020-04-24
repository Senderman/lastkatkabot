package com.senderman.lastkatkabot.usercommands

import callOf
import com.senderman.lastkatkabot.DBService
import com.senderman.lastkatkabot.LastkatkaBotHandler
import mock
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatchers.*
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import willDo
import willReturn

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class WeatherTest {

    lateinit var message: Message
    lateinit var handler: LastkatkaBotHandler
    lateinit var db: DBService
    lateinit var testingObject: Weather
    var sentMessage: String = ""

    @BeforeAll
    fun prepare() {
        db = mock(DBService::class)
        handler = mock(LastkatkaBotHandler::class)
        callOf(handler.sendMessage(anyLong(), anyString())) willDo {
            sentMessage = it.arguments[1].toString()
            mock(Message::class)
        }
        testingObject = Weather(handler, db)

        val user = mock(User::class)
        callOf(user.id) willReturn 123

        message = mock(Message::class)
        callOf(message.from) willReturn user
    }

    @BeforeEach
    fun resetDB() {
        callOf(db.getUserCity(anyInt())) willReturn null
    }

    @Test
    fun testSpecifiedCityWithEmptyDbRecord() {
        callOf(message.text) willReturn "/weather Москва"
        testingObject.execute(message)
        assert("Погода в Москве" in sentMessage)
    }

    @Test
    fun testSpecifiedCityWithExistingDbRecordWillIgnoreDb() {
        callOf(message.text) willReturn "/weather Москва"
        callOf(db.getUserCity(anyInt())) willReturn "/pogoda/mytischi"
        testingObject.execute(message)
        assert("Погода в Москве" in sentMessage)
    }

    @Test
    fun testNotSpecifiedCityWithEmptyDbRecord() {
        callOf(message.text) willReturn "/weather"
        testingObject.execute(message)
        assert("Вы не указали город" in sentMessage)
    }

    @Test
    fun testNotSpecifiedCityWithExistingDbRecord() {
        callOf(message.text) willReturn "/weather"
        callOf(db.getUserCity(anyInt())) willReturn "/pogoda/213"
        testingObject.execute(message)
        assert("Погода в Москве" in sentMessage)
    }

    @Test
    fun testNotExistingCity() {
        callOf(message.text) willReturn "/weather wfjwiojiowfjiow"
        testingObject.execute(message)
        assert(sentMessage == "Город не найден")
    }
}