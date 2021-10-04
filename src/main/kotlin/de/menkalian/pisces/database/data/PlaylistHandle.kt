package de.menkalian.pisces.database.data

import de.menkalian.pisces.database.IDatabaseHandler

/**
 * Handle mit Informationen zur Playlist.
 *
 * @property databaseHandler Instanz des databaseHandlers, der für interne Methodenaufrufe genutzt werden kann.
 * @property name Name der Playlist
 * @property guildId Discord-ID des Servers (Guild) auf der die Playlist angelegt wurde.
 */
data class PlaylistHandle internal constructor(
    val databaseHandler: IDatabaseHandler,
    val name: String,
    val guildId: Long
)
