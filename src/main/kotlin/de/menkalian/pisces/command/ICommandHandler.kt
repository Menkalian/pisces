package de.menkalian.pisces.command

import de.menkalian.pisces.IHandler
import de.menkalian.pisces.command.data.ECommandSource
import de.menkalian.pisces.util.Variables
import net.dv8tion.jda.api.entities.ChannelType

/**
 * Der [ICommandHandler] verwaltet die Commands
 */
interface ICommandHandler : IHandler {
    val commands: List<ICommand>

    fun executeGuildCommand(
        cmdString: String,
        guildId: Long,
        source: ECommandSource,
        channelId: Long,
        channelType: ChannelType,
        authorId: Long,
        additionalVars: Variables
    )

    fun executePrivateCommand(cmdString: String, authorId: Long, additionalVars: Variables)

    fun fireGuildPrefixChanged(guildId: Long, newPrefix: String)
    fun addGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)
    fun removeGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)

    fun interface IGuildPrefixChangedListener {
        fun onPrefixChanged(guildId: Long, newPrefix: String)
    }

}