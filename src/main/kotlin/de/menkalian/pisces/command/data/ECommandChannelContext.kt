package de.menkalian.pisces.command.data

import de.menkalian.pisces.command.data.ECommandChannelContext.*
import net.dv8tion.jda.api.entities.channel.ChannelType

/**
 * Kontext zur Beschreibung des Channels, von dem ein Befehl empfangen wurde.
 *
 * @property PRIVATE Privater Chat mit einem Nutzer
 * @property GUILD_ALL Jeglicher Textchat innerhalb eines Servers
 * @property GUILD_TEXT Reiner Textchat-Channel innerhalb eines Servers
 * @property GUILD_STORE Sog. `Store-Channel`. Besondere Form des Textchannels für affiliated Server.
 */
enum class ECommandChannelContext {
    PRIVATE, GUILD_ALL, GUILD_TEXT, GUILD_STORE;

    /**
     * Prüft ob der angegebene [ChannelType] durch diesen [ECommandChannelContext] abgedeckt wird.
     *
     * @param type JDA-[ChannelType] der geprüft werden soll.
     * @return Ob dieser [ChannelType] abgedeckt ist.
     */
    fun supports(type: ChannelType) =
        type.isMessage && when (this) {
            PRIVATE     -> type == ChannelType.PRIVATE
            GUILD_ALL   -> type.isGuild
            GUILD_TEXT  -> type == ChannelType.TEXT
            GUILD_STORE -> type == ChannelType.STAGE
        }
}