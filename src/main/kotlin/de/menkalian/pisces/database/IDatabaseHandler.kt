package de.menkalian.pisces.database

import de.menkalian.pisces.IHandler
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.database.data.DatabaseSongEntry
import de.menkalian.pisces.database.data.PlaylistHandle

/**
 * Schnittstelle zum Zugriff auf die Datenbanken/persistente Datenspeicherung.
 */
interface IDatabaseHandler : IHandler {
    // Command Shortcuts & Aliases
    fun addCommandShortcut(guildId: Long, alias: String, original: String)
    fun getFormalCommandName(guildId: Long, alias: String): String

    // Variables/Settings
    fun getSettingsValue(guildId: Long, variable: String, default: String = ""): String
    fun setSettingsValue(guildId: Long, variable: String, value: String)

    // Song (Information) Caching
    fun createSavedSongEntryIfNotExists(audioTrackInfo: TrackInfo) : Long
    fun getSavedSongEntryInformation(id: Long) : DatabaseSongEntry?
    fun clearAllUnreferencedSongEntries()

    // Playlists
    fun getOrCreatePlaylist(guildId: Long, name: String): PlaylistHandle
    fun addToPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo): Boolean
    fun removeFromPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo)
    fun deletePlaylist(handle: PlaylistHandle)

}