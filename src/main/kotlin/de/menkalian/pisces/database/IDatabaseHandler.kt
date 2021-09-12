package de.menkalian.pisces.database

import de.menkalian.pisces.IHandler

interface IDatabaseHandler : IHandler {
    // Command Shortcuts & Aliases
    fun addCommandShortcut(guildId: Long, alias: String, original: String)
    fun getFormalCommandName(guildId: Long, alias: String): String

    // Variables/Settings
    fun getSettingsValue(guildId: Long, variable: String, default: String = ""): String
    fun setSettingsValue(guildId: Long, variable: String, value: String)
}