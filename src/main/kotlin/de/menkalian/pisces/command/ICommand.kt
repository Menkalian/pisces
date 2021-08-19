package de.menkalian.pisces.command

import net.dv8tion.jda.api.entities.ChannelType

interface ICommand {
    enum class ECommandChannelContext {
        PRIVATE,
        GUILD_ALL,
        GUILD_TEXT,
        GUILD_STORE;

        fun supports(type: ChannelType) =
            type.isMessage && when (this) {
                PRIVATE     -> type == ChannelType.PRIVATE
                GUILD_ALL   -> type.isGuild
                GUILD_TEXT  -> type == ChannelType.TEXT
                GUILD_STORE -> type == ChannelType.STORE
            }
    }

    val supportedContexts: List<ECommandChannelContext>
    infix fun supports(type: ChannelType) =
        supportedContexts.any { it.supports(type) }


}