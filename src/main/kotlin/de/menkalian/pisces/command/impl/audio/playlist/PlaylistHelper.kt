package de.menkalian.pisces.command.impl.audio.playlist

import de.menkalian.pisces.message.IMessageHandler
import de.menkalian.pisces.util.withErrorColor

object PlaylistHelper {
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