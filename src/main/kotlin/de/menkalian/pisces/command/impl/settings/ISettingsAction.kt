package de.menkalian.pisces.command.impl.settings

interface ISettingsAction {
    fun get(): String
    fun notifyCurrent()
    fun set(value: String)
}