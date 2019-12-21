package com.senderman

import org.telegram.telegrambots.meta.api.objects.User

open class TgUser {

    val id: Int
    val name: String
    val link: String
        get() = "<a href=\"tg://user?id=$id\">$name</a>"

    constructor(id: Int, name: String) {
        this.id = id
        this.name = getSafeName(name)
    }

    constructor(user: User) {
        this.id = user.id
        this.name = getSafeName(user.firstName)
    }

    private fun getSafeName(name: String): String {
        if (name.trim().isBlank()) return "Без имени"
        return name
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&", "&amp;")
    }
}