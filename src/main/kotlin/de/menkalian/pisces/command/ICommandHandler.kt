package de.menkalian.pisces.command

import de.menkalian.pisces.IHandler

/**
 * Der [ICommandHandler] verwaltet die Commands
 */
interface ICommandHandler : IHandler {
    val commands: List<ICommand>
    val commandsByName: Map<String, ICommand>

    fun getCommand(name: String, guildId: Long = 0L): ICommand?

    fun fireGuildPrefixChanged(guildId: Long, newPrefix: String)
    fun addGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)
    fun removeGuildPrefixChangedListener(listener: IGuildPrefixChangedListener)

    fun interface IGuildPrefixChangedListener {
        fun onPrefixChanged(guildId: Long, newPrefix: String)
    }

}