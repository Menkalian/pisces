package de.menkalian.pisces.database

import de.menkalian.pisces.IHandler
import de.menkalian.pisces.audio.data.TrackInfo
import de.menkalian.pisces.database.data.DatabaseSongEntry
import de.menkalian.pisces.database.data.PlaylistHandle

/**
 * Schnittstelle zum Zugriff auf die Datenbanken/persistente Datenspeicherung.
 *
 * Aktuell werden die folgenden Informationen in der Datenbank abgelegt:
 *  - Aliase/Alternativnamen für Commands (allgemein und serverspezifisch)
 *  - Einstellungen (allgemein und serverspezifisch)
 *  - Informationen für ausgewählte Songs, die in Playlists oder für Joinsounds verwendet werden
 *  - Playlists (serverspezifisch)
 *  - Joinsounds (benutzerspezifisch)
 */
interface IDatabaseHandler : IHandler {
    // Command Shortcuts & Aliases
    /**
     * Fügt ein Alias/einen Alternativnamen für ein Command hinzu.
     *
     * @param guildId Server-ID, für die der Alternativname hinzugefügt werden soll. Eine ID `0` zeigt die Allgemeingültigkeit des Alias an.
     * @param alias Alias/Alternativname, der hinzugefügt werden soll
     * @param original Originaler Name des Commands. Dies kann auch ein bestehendes Alias sein, das dann auf den formalen Namen aufgelöst wird.
     */
    fun addCommandShortcut(guildId: Long, alias: String, original: String)

    /**
     * Löst das angegebene Alias auf und gibt den formalen/korrekten Namen des Commands zurück.
     *
     * @param guildId Server-ID, für die der Lookup durchgeführt werden soll. Eine ID `0` zeigt an, dass nur allgemeine Alternativnamen berücksichtigt werden sollen.
     * @param alias Alias/Alternativname, der aufgelöst werden soll
     * @return Aufgelöster korrekter Command-Name.
     */
    fun getFormalCommandName(guildId: Long, alias: String): String

    // Variables/Settings
    /**
     * Setzt einen Einstellungswert für einen speziellen Server.
     *
     * @param guildId Server-ID, für die die Einstellung gesetzt werden soll. Eine ID `0` zeigt die Allgemeingültigkeit der Einstellung an.
     * @param variable Schlüssel, der genutzt wird, um die Einstellung zu identifizieren
     * @param value Wert, der gesetzt werden soll.
     */
    fun setSettingsValue(guildId: Long, variable: String, value: String)

    /**
     * Liest einen Einstellungswert für einen speziellen Server.
     * Sollte für diesen Server die Einstellung nicht explizit gesetzt sein, wird der Standardwert der Einstellung gelesen.
     *
     * @param guildId Server-ID, für die die Einstellung gelesen werden soll. Eine ID `0` zeigt die Allgemeingültigkeit der Einstellung an.
     * @param variable Schlüssel, der genutzt wird, um die Einstellung zu identifizieren.
     * @return Gesetzter Wert.
     */
    fun getSettingsValue(guildId: Long, variable: String, default: String = ""): String

    // Song (Information) Caching
    /**
     * Legt einen SongEntry für das angegebene [TrackInfo]-Objekt an.
     *
     * @param audioTrackInfo Informationen, die in der Datenbank gespeichert werden sollen.
     * @return Die generierte ID des Songs in der Datenbank.
     */
    fun createSavedSongEntryIfNotExists(audioTrackInfo: TrackInfo): Long

    /**
     * Liest die Informationen aus der Datenbank, die hinter der angegebenen ID liegen.
     *
     * @param id Die ID des Songs in der Datenbank
     * @return Der gefundene SongEntry oder null, falls kein Eintrag mit der ID existiert.
     */
    fun getSavedSongEntryInformation(id: Long): DatabaseSongEntry?

    /**
     * Löscht alle SongEntries aus der Datenbank, die nicht in einer Playlist sind oder für einen User-JoinSound verwendet werden.
     */
    fun clearAllUnreferencedSongEntries()

    // Playlists
    /**
     * Legt eine neue Playlist an, oder gibt ein Handle zurück, das die existierende Playlist referenziert.
     *
     * @param guildId Server-ID des Servers für den die Playlist angelegt werden soll.
     * @param name Name der Playlist, einmalig pro Server.
     * @return Handle für die Playlist
     */
    fun getOrCreatePlaylist(guildId: Long, name: String): PlaylistHandle

    /**
     * Gibt die Playlist mit dem Namen zurück falls diese existiert.
     *
     * @param guildId Server-ID des Servers für den die Playlist angelegt werden soll.
     * @param name Name der Playlist, einmalig pro Server.
     * @return Handle für die Playlist
     */
    fun getPlaylistIfExists(guildId: Long, name: String): PlaylistHandle?

    /**
     * Gibt eine Liste der SongEntries zurück, die für die Playlist hinterlegt sind.
     *
     * @param handle Handle für die Playlist
     * @return Liste der Songs. Kann leer sein, wenn die Playlist nicht existiert (oder einfach keine Songs enthält).
     */
    fun getPlaylistSongs(handle: PlaylistHandle): List<DatabaseSongEntry>

    /**
     * Fügt den angegebenen Track zur Playlist hinzu.
     *
     * @param handle Handle für die Playlist
     * @param audioTrackInfo Informationen zum Track, der hinzugefügt werden soll.
     * @return Ob der Song erfolgreich hinzugefügt wurde
     */
    fun addToPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo): Boolean

    /**
     * Entfernt den angegebenen Track aus der Playlist.
     *
     * @param handle Handle für die Playlist
     * @param audioTrackInfo Informationen zum Track, der entfernt werden soll.
     */
    fun removeFromPlaylist(handle: PlaylistHandle, audioTrackInfo: TrackInfo)

    /**
     * Löscht die angegebene Playlist.
     *
     * @param handle Handle für die Playlist
     */
    fun deletePlaylist(handle: PlaylistHandle)

    // Joinsound
    /**
     * Setzt den JoinSound für den angegebenen Nutzer.
     *
     * @param userId Discord-ID des Nutzers
     * @param audioTrackInfo Informationen zum Track, der gesetzt werden soll.
     */
    fun setUserJoinsound(userId: Long, audioTrackInfo: TrackInfo)

    /**
     * Liest den JoinSound für den angegebenen Nutzer aus der Datenbank.
     *
     * @param userId Discord-ID des Nutzers
     * @return [DatabaseSongEntry]-Objekt, das für den Nutzer hinterlegt ist oder null, falls keine Daten für den Nutzer gespeichert wurden.
     */
    fun getUserJoinsound(userId: Long): DatabaseSongEntry?
}