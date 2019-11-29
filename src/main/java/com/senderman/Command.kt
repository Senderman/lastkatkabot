package com.senderman

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Command(
        val name: String,
        val desc: String,
        val showInHelp: Boolean = true,
        val forAllAdmins: Boolean = false,
        val forPremium: Boolean = false,
        val forMainAdmin: Boolean = false
)