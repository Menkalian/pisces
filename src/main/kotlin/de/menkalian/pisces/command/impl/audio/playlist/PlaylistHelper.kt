package de.menkalian.pisces.command.impl.audio.playlist

import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.withErrorColor

/**
 * Hilfsobjekt für die Behandlung von Playlists
 */
object PlaylistHelper {
    /**
     * Prüft ob der angegebene Playlistname gültig ist und sendet gegebenenfalls eine entsprechende Fehlermeldung an den Nutzer
     *
     * @param name Name der Playlist, der geprüft werden soll
     * @param guildId DiscordID des Servers
     * @param channelId DiscordID des Channels
     * @param messageHandler Aktive [IMessageHandler]-Instanz
     *
     * @return Ob der angegebene Name gültig ist.
     */
    fun ensurePlaylistValid(
        name: String,
        guildId: Long, channelId: Long,
        messageHandler: IMessageHandler
    ): Boolean {
        if (name.isValidName().not()) {
            messageHandler
                .createMessage(guildId, channelId)
                .withTitle("Name \"$name\" ist ungültig für eine Playlist.")
                .withErrorColor()
                .build()
            return false
        } else {
            return true
        }
    }

    private fun String.isValidName(): Boolean {
        return isNotBlank()
    }
}